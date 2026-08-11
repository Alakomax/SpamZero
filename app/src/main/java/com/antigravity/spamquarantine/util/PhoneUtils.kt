package com.antigravity.spamquarantine.util

import java.util.regex.Pattern

object PhoneUtils {

    /**
     * Normaliza un número telefónico chileno a formato E.164 (+56XXXXXXXXX).
     * Ejemplos:
     * "600 716 4000" -> "+566007164000"
     * "(80) 902 8449" -> "+56809028449"
     * "+56 9 2882 8730" -> "+56928828730"
     * "+56 600 338 0002" -> "+566003380002"
     */
    fun normalizeChilePhoneNumber(rawNumber: String?): String {
        if (rawNumber.isNullOrBlank()) return ""

        // Eliminar caracteres no numéricos excepto el signo + al inicio
        var digits = rawNumber.replace(Regex("[^0-9+]"), "")

        if (digits.startsWith("+56")) {
            return digits
        }

        if (digits.startsWith("56")) {
            return "+$digits"
        }

        // Si empieza con 9 o 2 o 600 o 800 o 80
        if (digits.startsWith("9") && digits.length == 9) {
            return "+56$digits"
        }

        if ((digits.startsWith("600") || digits.startsWith("800") || digits.startsWith("80")) && digits.length >= 8) {
            return "+56$digits"
        }

        return if (digits.startsWith("+")) digits else "+56$digits"
    }

    /**
     * Verifica si un número formateado en E.164 o texto coincide con una expresión regular dada.
     */
    fun matchesRegexPattern(textOrNumber: String, regexPattern: String): Boolean {
        if (textOrNumber.isBlank() || regexPattern.isBlank()) return false
        return try {
            val pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE)
            pattern.matcher(textOrNumber).find()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lista de patrones de expresiones regulares predeterminados para combatir el spam de llamadas y SMS en Chile.
     */
    fun getDefaultChileSpamPatterns(): List<Pair<String, String>> {
        return listOf(
            Pair("^\\+56(600|800|809)\\d+", "Servicios Comerciales y Cobro Revertido (600 / 800 / 809)"),
            Pair("^\\+569(2882|4434|4433|4435)\\d{4}$", "Bloque Móvil Call Center Chile (2882 / 4434 / 4433 / 4435)"),
            Pair("^\\+5680\\d+", "Prefijos Especiales de Cobranza (80 XXX XXXX)"),
            Pair("^\\+5622\\d{7}$", "Telemarketing Fijo Santiago (22 XXX XXXX)"),
            Pair("^\\+56(44|43|42|45|41)\\d+", "Rango Números Comerciales (44 / 4X VoIP)"),
            Pair("^\\+?34931\\d+$", "Spam Internacional España / Casino (+34 931)"),
            Pair("^44\\d+$", "Troncales VoIP Estafas SMS (44 XXX XXXX)"),
            Pair("(?i)(joker jewels|fortune (tiger|rabbit)|gates of olympus|7k|apuestas|ruleta|giros)", "Filtro SMS: Spam Casino / Apuestas"),
            Pair("(?i)(multa pendiente|aviso tag|copec|ultimo aviso|puntos disponibles|abonado.*pesos)", "Filtro SMS: Estafas TAG / Copec / Bancos")
        )
    }
}
