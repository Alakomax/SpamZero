package com.alakomax.spamzero.util

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.alakomax.spamzero.data.db.AppDatabase

data class ImportedBlockSuggestion(
    val id: String,
    val title: String,
    val subtitle: String,
    val pattern: String,
    val category: String,
    val description: String,
    val sampleCount: Int
)

object SystemBlocklistImporter {

    suspend fun scanSystemBlockedItems(context: Context): List<ImportedBlockSuggestion> {
        val suggestions = mutableListOf<ImportedBlockSuggestion>()
        val countryInfo = CountryUtils.getSimCountryInfo(context)

        val savedContactNumbers = getSavedContactNumbers(context)
        val rawBlockedNumbers = mutableSetOf<String>()

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            try {
                val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.TYPE)
                val selection = "${CallLog.Calls.TYPE} = ? OR ${CallLog.Calls.TYPE} = ?"
                val selectionArgs = arrayOf(
                    CallLog.Calls.BLOCKED_TYPE.toString(),
                    CallLog.Calls.REJECTED_TYPE.toString()
                )
                context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    "${CallLog.Calls.DATE} DESC"
                )?.use { cursor ->
                    val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                    while (cursor.moveToNext()) {
                        if (numberIndex >= 0) {
                            val num = cursor.getString(numberIndex)
                            if (!num.isNullOrBlank()) {
                                rawBlockedNumbers.add(num.trim())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SystemBlocklistImporter", "Error leyendo CallLog: ${e.message}")
            }
        }

        try {
            if (BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                val projection = arrayOf(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                context.contentResolver.query(
                    BlockedNumberContract.BlockedNumbers.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    val numIdx = cursor.getColumnIndex(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                    while (cursor.moveToNext()) {
                        if (numIdx >= 0) {
                            val num = cursor.getString(numIdx)
                            if (!num.isNullOrBlank()) {
                                rawBlockedNumbers.add(num.trim())
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SystemBlocklistImporter", "Error leyendo BlockedNumberContract: ${e.message}")
        }

        val filteredNumbers = rawBlockedNumbers.filterNot { rawNum ->
            val norm = PhoneUtils.normalizePhoneNumber(rawNum, countryInfo.code)
            savedContactNumbers.contains(rawNum) || savedContactNumbers.contains(norm)
        }

        if (filteredNumbers.isEmpty()) {
            return emptyList()
        }

        val prefixGroups = mutableMapOf<String, MutableList<String>>()
        val individualNumbers = mutableListOf<String>()

        for (rawNum in filteredNumbers) {
            val norm = PhoneUtils.normalizePhoneNumber(rawNum, countryInfo.code)
            val digitsOnly = norm.replace(Regex("[^0-9]"), "")
            if (digitsOnly.length >= 8) {
                val prefix = digitsOnly.take(7)
                prefixGroups.getOrPut(prefix) { mutableListOf() }.add(norm)
            } else {
                individualNumbers.add(norm)
            }
        }

        for ((prefix, list) in prefixGroups) {
            if (list.size >= 2) {
                val sample = list.first()
                val cleanPrefix = sample.take(minOf(sample.length, 8)).replace(Regex("[\\s\\-\\(\\)]"), "")
                val regexPattern = "^\\+?${cleanPrefix.replace("+", "")}.*"
                suggestions.add(
                    ImportedBlockSuggestion(
                        id = "prefix_$prefix",
                        title = "Central telefónica sospechosa ($cleanPrefix...)",
                        subtitle = "${list.size} números bloqueados que coinciden en los primeros dígitos",
                        pattern = regexPattern,
                        category = "📞 Llamadas Importadas",
                        description = "Bloquea llamadas de centrales telefónicas detectadas en tus registros",
                        sampleCount = list.size
                    )
                )
            } else {
                individualNumbers.addAll(list)
            }
        }

        val distinctIndividuals = individualNumbers.distinct()
        if (distinctIndividuals.isNotEmpty()) {
            val count = distinctIndividuals.size
            val escapedNumbers = distinctIndividuals.map { Regex.escape(it.replace(Regex("[\\s\\-\\(\\)]"), "")) }
            val combinedPattern = "^\\+?(${escapedNumbers.joinToString("|")})$"
            suggestions.add(
                ImportedBlockSuggestion(
                    id = "individual_numbers",
                    title = "Números bloqueados en tu teléfono ($count)",
                    subtitle = "Lista de números marcados como no deseados en tu app de llamadas",
                    pattern = combinedPattern,
                    category = "📞 Llamadas Importadas",
                    description = "Bloquea números individuales importados de tu lista negra del sistema",
                    sampleCount = count
                )
            )
        }

        try {
            val db = AppDatabase.getDatabase(context)
            val smsLogs = db.smsQuarantineDao().getAllSmsLogs()
            if (smsLogs.isNotEmpty()) {
                val keywords = listOf("preaprobado", "ganaste", "premio", "casino", "inversión", "prestamo", "banco", "urgente")
                val foundKeywords = mutableSetOf<String>()
                for (sms in smsLogs) {
                    val lowerBody = sms.messageBody.lowercase()
                    for (kw in keywords) {
                        if (lowerBody.contains(kw)) {
                            foundKeywords.add(kw)
                        }
                    }
                }
                if (foundKeywords.isNotEmpty()) {
                    val kwPattern = "(?i).*(${foundKeywords.joinToString("|")}).*"
                    suggestions.add(
                        ImportedBlockSuggestion(
                            id = "sms_keywords",
                            title = "Palabras clave en SMS sospechosos",
                            subtitle = "Filtra textos con términos recurrentes (${foundKeywords.take(3).joinToString(", ")})",
                            pattern = kwPattern,
                            category = "📩 SMS Importados",
                            description = "Bloquea notificaciones SMS de fraudes frecuentes",
                            sampleCount = smsLogs.size
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SystemBlocklistImporter", "Error analizando SMS en cuarentena: ${e.message}")
        }

        return suggestions
    }

    private fun getSavedContactNumbers(context: Context): Set<String> {
        val numbers = mutableSetOf<String>()
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return emptySet()
        }
        return try {
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->
                val idx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    if (idx >= 0) {
                        val num = cursor.getString(idx)
                        if (!num.isNullOrBlank()) {
                            numbers.add(num.trim())
                        }
                    }
                }
            }
            numbers
        } catch (e: Exception) {
            Log.e("SystemBlocklistImporter", "Error leyendo contactos guardados: ${e.message}")
            emptySet()
        }
    }
}
