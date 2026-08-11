package com.antigravity.spamquarantine.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsMessage
import android.util.Log
import com.antigravity.spamquarantine.data.db.AppDatabase
import com.antigravity.spamquarantine.data.model.SmsQuarantineLogEntity
import com.antigravity.spamquarantine.util.PhoneUtils
import com.antigravity.spamquarantine.util.ProtectionPreferences
import com.antigravity.spamquarantine.util.SpamRuleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsSpamReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.provider.Telephony.SMS_RECEIVED") return

        // Verificar si la protección está activada por el usuario
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

        val normalizedSender = PhoneUtils.normalizeChilePhoneNumber(senderNumber)
        Log.d("SmsSpamReceiver", "SMS entrante de: $senderNumber (E164: $normalizedSender) | Texto: $fullBody")

        // 1. Verificar si el remitente está en la agenda de contactos (Lista Blanca automática)
        if (isContact(context, senderNumber) || isContact(context, normalizedSender)) {
            Log.d("SmsSpamReceiver", "Remitente $normalizedSender está en contactos. Permitido.")
            return
        }

        // 2. Evaluar reglas activas contra el número de teléfono y el contenido del texto
        val activeRules = SpamRuleCache.getActiveRulesSync(context.applicationContext)

        var matchedRulePattern: String? = null
        for (rule in activeRules) {
            if (PhoneUtils.matchesRegexPattern(normalizedSender, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(senderNumber, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(fullBody, rule.pattern)) {
                matchedRulePattern = rule.pattern
                break
            }
        }

        if (matchedRulePattern != null) {
            Log.w("SmsSpamReceiver", "SMS SPAM DETECTADO de $normalizedSender por patrón $matchedRulePattern")

            receiverScope.launch {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    db.smsQuarantineDao().insertSmsLog(
                        SmsQuarantineLogEntity(
                            senderPhoneNumber = senderNumber,
                            messageBody = fullBody,
                            matchedPattern = matchedRulePattern
                        )
                    )
                } catch (e: Exception) {
                    Log.e("SmsSpamReceiver", "Error insertando SMS en cuarentena: ${e.message}")
                }
            }
        }
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
