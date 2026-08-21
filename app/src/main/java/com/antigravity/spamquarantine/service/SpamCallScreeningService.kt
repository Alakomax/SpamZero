package com.antigravity.spamquarantine.service

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.antigravity.spamquarantine.data.db.AppDatabase
import com.antigravity.spamquarantine.data.model.QuarantineLogEntity
import com.antigravity.spamquarantine.data.model.RuleEntity
import com.antigravity.spamquarantine.util.CountryUtils
import com.antigravity.spamquarantine.util.PhoneUtils
import com.antigravity.spamquarantine.util.ProtectionPreferences
import com.antigravity.spamquarantine.util.SpamRuleCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SpamCallScreeningService : CallScreeningService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val rawHandle: Uri? = callDetails.handle
        val rawNumber: String = rawHandle?.schemeSpecificPart ?: ""

        if (rawNumber.isBlank()) {
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        // Verificar si la protección está activada por el usuario
        if (!ProtectionPreferences.isProtectionEnabled(applicationContext)) {
            Log.d("SpamScreening", "Protección deshabilitada por el usuario. Llamada permitida: $rawNumber")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val countryInfo = CountryUtils.getSimCountryInfo(applicationContext)
        val normalizedNumber = PhoneUtils.normalizePhoneNumber(rawNumber, countryInfo.code)
        Log.d("SpamScreening", "Llamada entrante evaluada [${countryInfo.code} ${countryInfo.flagEmoji}]: Raw=$rawNumber -> E.164=$normalizedNumber")

        // 1. Lista Blanca Automática: Verificar si está en Contactos del dispositivo
        if (isContact(this, rawNumber) || isContact(this, normalizedNumber)) {
            Log.d("SpamScreening", "Número $normalizedNumber está en Contactos. Permitido.")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        // 2. Obtener reglas activas en RAM de forma ultrarrápida
        val activeRules = SpamRuleCache.getActiveRulesSync(applicationContext)

        val sanitizedRaw = rawNumber.replace(Regex("[\\s\\-\\(\\)]"), "")
        val sanitizedNorm = normalizedNumber.replace(Regex("[\\s\\-\\(\\)]"), "")

        var matchedRule: RuleEntity? = null
        for (rule in activeRules) {
            if (PhoneUtils.matchesRegexPattern(normalizedNumber, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(rawNumber, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedNorm, rule.pattern) ||
                PhoneUtils.matchesRegexPattern(sanitizedRaw, rule.pattern)) {
                matchedRule = rule
                break
            }
        }

        if (matchedRule != null) {
            Log.w("SpamScreening", "LLAMADA SPAM BLOQUEADA (0 REPIQUES): $normalizedNumber por patrón ${matchedRule.pattern}")

            // Responder a Android Telecom INMEDIATAMENTE: Silenciar ringer y cortar llamada (0 repiques)
            val responseBuilder = CallResponse.Builder()
                .setDisallowCall(true)  // Bloquear llamada
                .setRejectCall(true)    // Rechazar/cortar línea
                .setSkipCallLog(false)  // Conservar en log para auditoría

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBuilder.setSilenceCall(true) // Silenciar timbre al instante (0 repiques)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                responseBuilder.setSkipNotification(true) // Ocultar notificación emergente
            }

            respondToCall(callDetails, responseBuilder.build())

            // Registrar en la base de datos de Cuarentena en segundo plano (asíncrono)
            val matchedPattern = matchedRule.pattern
            serviceScope.launch {
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    db.quarantineDao().insertLog(
                        QuarantineLogEntity(
                            rawPhoneNumber = rawNumber,
                            normalizedPhoneNumber = normalizedNumber,
                            matchedPattern = matchedPattern
                        )
                    )
                } catch (e: Exception) {
                    Log.e("SpamScreening", "Error registrando llamada en Cuarentena: ${e.message}")
                }
            }
        } else {
            Log.d("SpamScreening", "Llamada $normalizedNumber permitida (sin coincidencia de spam).")
            respondToCall(callDetails, CallResponse.Builder().build())
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
            Log.e("SpamScreening", "Error al consultar Contactos: ${e.message}")
            false
        }
    }
}
