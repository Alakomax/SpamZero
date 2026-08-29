package com.alakomax.spamzero.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.alakomax.spamzero.MainActivity
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.SmsQuarantineLogEntity
import com.alakomax.spamzero.util.CountryUtils
import com.alakomax.spamzero.util.PhoneUtils
import com.alakomax.spamzero.util.ProtectionPreferences
import com.alakomax.spamzero.util.SpamRuleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val knownSmsPackages = setOf(
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.android.mms",
        "com.oneplus.mms",
        "com.xiaomi.simactivate.service",
        "com.miui.smsextra"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val sbnNotif = sbn ?: return
        val packageName = sbnNotif.packageName ?: return
        if (!ProtectionPreferences.isProtectionEnabled(applicationContext)) return
        val notif = sbnNotif.notification ?: return
        val category = notif.category
        val isSmsApp = knownSmsPackages.contains(packageName) || category == Notification.CATEGORY_MESSAGE
        if (!isSmsApp) return
        val extras = notif.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val fullBody = if (bigText.isNotBlank()) bigText else text
        if (title.isBlank() && fullBody.isBlank()) return
        val countryInfo = CountryUtils.getSimCountryInfo(applicationContext)
        val normalizedSender = PhoneUtils.normalizePhoneNumber(title, countryInfo.code)
        if (isContact(applicationContext, title) || isContact(applicationContext, normalizedSender)) {
            Log.d("SmsNotificationListener", "Remitente $title en Contactos. Notificación permitida.")
            return
        }
        val sanitizedSender = title.replace(Regex("[\\s\\-\\(\\)]"), "")
        val sanitizedNorm = normalizedSender.replace(Regex("[\\s\\-\\(\\)]"), "")
        val activeRules = SpamRuleCache.getActiveRulesSync(applicationContext)
        var matchedRulePattern: String? = null
        for (rule in activeRules) {
            val matchesSender = (rule.category != "Texto SMS") && (
                PhoneUtils.matchesRegexPattern(normalizedSender, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(title, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedNorm, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedSender, rule.pattern)
            )
            val matchesBody = (rule.category == "Texto SMS") && PhoneUtils.matchesRegexPattern(fullBody, rule.pattern)
            if (matchesSender || matchesBody) {
                matchedRulePattern = rule.pattern
                break
            }
        }
        if (matchedRulePattern != null) {
            Log.w("SmsNotificationListener", "SMS SPAM DETECTADO en notificación de $packageName de $title")
            try {
                cancelNotification(sbnNotif.key)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cancelNotification(sbnNotif.packageName, sbnNotif.tag, sbnNotif.id)
                }
            } catch (e: Exception) {
                Log.e("SmsNotificationListener", "Error al cancelar notificación: ${e.message}")
            }
            serviceScope.launch {
                try {
                    val isDuplicate = com.alakomax.spamzero.util.SmsDeduplicator.isDuplicateAndMark(title, fullBody)
                    if (!isDuplicate) {
                        val db = AppDatabase.getDatabase(applicationContext)
                        val recentCount = db.smsQuarantineDao().countRecentDuplicates(title, fullBody, System.currentTimeMillis() - 10000)
                        if (recentCount == 0) {
                            db.smsQuarantineDao().insertSmsLog(
                                SmsQuarantineLogEntity(
                                    senderPhoneNumber = title,
                                    messageBody = fullBody,
                                    matchedPattern = matchedRulePattern
                                )
                            )
                            showSpamNotification(applicationContext, title, fullBody, matchedRulePattern)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SmsNotificationListener", "Error insertando SMS en cuarentena: ${e.message}")
                }
            }
        }
    }

    private fun isContact(context: Context, number: String): Boolean {
        if (number.isBlank()) return false
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
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
            Log.e("SmsNotificationListener", "Error al consultar Contactos: ${e.message}")
            false
        }
    }

    private fun showSpamNotification(context: Context, sender: String, body: String, pattern: String) {
        val channelId = "spam_alerts_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de SMS Spam",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando SpamZero intercepta un mensaje sospechoso"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_tab", 1)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snippet = if (body.length > 60) body.take(60) + "..." else body
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 SMS Sospechoso Interceptado (SpamZero)")
            .setContentText("De: $sender | $snippet")
            .setStyle(NotificationCompat.BigTextStyle().bigText("De: $sender\n\n$body\n\nCoincide con patrón: $pattern"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
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
}
