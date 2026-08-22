package com.alakomax.spamzero.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsMessage
import android.util.Log
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.SmsQuarantineLogEntity
import com.alakomax.spamzero.util.CountryUtils
import com.alakomax.spamzero.util.PhoneUtils
import com.alakomax.spamzero.util.ProtectionPreferences
import com.alakomax.spamzero.util.SpamRuleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSpamReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        if (!ProtectionPreferences.isProtectionEnabled(context.applicationContext)) {
            Log.d("SmsSpamReceiver", "Protección desactivada. SMS ignorado.")
            return
        }

        val bundle = intent.extras ?: return
        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: return
        val format = bundle.getString("format")

        val messages = mutableListOf<SmsMessage>()
        for (pdu in pdus) {
            val pduBytes = pdu as? ByteArray ?: continue
            val message = if (format != null) {
                SmsMessage.createFromPdu(pduBytes, format)
            } else {
                @Suppress("DEPRECATION")
                SmsMessage.createFromPdu(pduBytes)
            }
            if (message != null) {
                messages.add(message)
            }
        }

        if (messages.isEmpty()) return

        val senderNumber = messages[0].originatingAddress ?: ""
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        if (senderNumber.isBlank() || fullBody.isBlank()) return

        val countryInfo = CountryUtils.getSimCountryInfo(context.applicationContext)
        val normalizedSender = PhoneUtils.normalizePhoneNumber(senderNumber, countryInfo.code)
        Log.d("SmsSpamReceiver", "SMS entrante [${countryInfo.code} ${countryInfo.flagEmoji}] de: $senderNumber (Norm: $normalizedSender) | Texto: $fullBody")

        if (isContact(context, senderNumber) || isContact(context, normalizedSender)) {
            Log.d("SmsSpamReceiver", "Remitente $normalizedSender está en Contactos. SMS permitido.")
            return
        }

        val activeRules = SpamRuleCache.getActiveRulesSync(context.applicationContext)

        val sanitizedSender = senderNumber.replace(Regex("[\\s\\-\\(\\)]"), "")
        val sanitizedNorm = normalizedSender.replace(Regex("[\\s\\-\\(\\)]"), "")

        var matchedRulePattern: String? = null
        for (rule in activeRules) {
            if (PhoneUtils.matchesRegexPattern(normalizedSender, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(senderNumber, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedNorm, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedSender, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(fullBody, rule.pattern)) {
                matchedRulePattern = rule.pattern
                break
            }
        }

        if (matchedRulePattern != null) {
            Log.w("SmsSpamReceiver", "SMS SPAM DETECTADO de $normalizedSender por patrón $matchedRulePattern")

            try {
                abortBroadcast()
            } catch (e: Exception) {
                Log.d("SmsSpamReceiver", "abortBroadcast no soportado en esta versión de Android/App: ${e.message}")
            }

            receiverScope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val recentCount = db.smsQuarantineDao().countRecentDuplicates(senderNumber, fullBody, System.currentTimeMillis() - 10000)
                    if (recentCount == 0) {
                        db.smsQuarantineDao().insertSmsLog(
                            SmsQuarantineLogEntity(
                                senderPhoneNumber = senderNumber,
                                messageBody = fullBody,
                                matchedPattern = matchedRulePattern
                            )
                        )
                        showSpamNotification(context.applicationContext, senderNumber, fullBody, matchedRulePattern)
                    }
                } catch (e: Exception) {
                    Log.e("SmsSpamReceiver", "Error insertando SMS en cuarentena: ${e.message}")
                }
            }
        }
    }

    private fun showSpamNotification(context: Context, sender: String, body: String, pattern: String) {
        val channelId = "spam_alerts_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Alertas de SMS Spam",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando SpamZero intercepta un mensaje sospechoso"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, com.alakomax.spamzero.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_tab", 1)
        }

        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val snippet = if (body.length > 60) body.take(60) + "..." else body

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SMS Sospechoso Interceptado (SpamZero)")
            .setContentText("De: $sender | $snippet")
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText("De: $sender\n\n$body\n\nCoincide con patrón: $pattern"))
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Ver en Cuarentena",
                pendingIntent
            )
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun isContact(context: Context, number: String): Boolean {
        if (number.isBlank()) return false
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup._ID)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.count > 0
            } ?: false
        } catch (e: Exception) {
            Log.e("SmsSpamReceiver", "Error consultando contactos: ${e.message}")
            false
        }
    }
}
