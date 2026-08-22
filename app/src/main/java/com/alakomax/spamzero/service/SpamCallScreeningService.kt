package com.alakomax.spamzero.service

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.QuarantineLogEntity
import com.alakomax.spamzero.data.model.RuleEntity
import com.alakomax.spamzero.util.CountryUtils
import com.alakomax.spamzero.util.PhoneUtils
import com.alakomax.spamzero.util.ProtectionPreferences
import com.alakomax.spamzero.util.SpamRuleCache
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

        if (!ProtectionPreferences.isProtectionEnabled(applicationContext)) {
            Log.d("SpamScreening", "Protección deshabilitada por el usuario. Llamada permitida: $rawNumber")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

        val countryInfo = CountryUtils.getSimCountryInfo(applicationContext)
        val normalizedNumber = PhoneUtils.normalizePhoneNumber(rawNumber, countryInfo.code)
        Log.d("SpamScreening", "Llamada entrante evaluada [${countryInfo.code} ${countryInfo.flagEmoji}]: Raw=$rawNumber -> E.164=$normalizedNumber")

        if (isContact(this, rawNumber) || isContact(this, normalizedNumber)) {
            Log.d("SpamScreening", "Número $normalizedNumber está en Contactos. Permitido.")
            respondToCall(callDetails, CallResponse.Builder().build())
            return
        }

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

            val responseBuilder = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                responseBuilder.setSilenceCall(true)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                responseBuilder.setSkipNotification(true)
            }

            respondToCall(callDetails, responseBuilder.build())

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
