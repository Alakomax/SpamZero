package com.antigravity.spamquarantine.util

import java.util.regex.Pattern

object PhoneUtils {

    /**
     * Normaliza un número telefónico a formato E.164 o limpiando caracteres espurios.
     * Elimina ceros iniciales de discado nacional (ej: "0943432103" -> "+56943432103").
     */
    fun normalizeChilePhoneNumber(rawNumber: String?): String {
        return normalizePhoneNumber(rawNumber, "CL")
    }

    fun normalizePhoneNumber(rawNumber: String?, countryIso: String = "CL"): String {
        if (rawNumber.isNullOrBlank()) return ""

        // Eliminar todo excepto dígitos y signo +
        var digits = rawNumber.replace(Regex("[^0-9+]"), "")

        if (digits.startsWith("+")) {
            return digits
        }

        val country = CountryUtils.getCountryByCode(countryIso)
        val prefixNoPlus = country.dialCode.removePrefix("+") // ej: "56"

        if (digits.startsWith(prefixNoPlus)) {
            return "+$digits"
        }

        // Si empieza con 0 nacional (ej: 0943432103 -> 943432103)
        if (digits.startsWith("0") && digits.length > 1) {
            digits = digits.removePrefix("0")
        }

        // Caso Chile (+56)
        if (countryIso.equals("CL", ignoreCase = true) || prefixNoPlus == "56") {
            if (digits.startsWith("9") && digits.length == 9) {
                return "+56$digits"
            }
            if ((digits.startsWith("600") || digits.startsWith("800") || digits.startsWith("80") || digits.startsWith("809")) && digits.length >= 8) {
                return "+56$digits"
            }
            return "+56$digits"
        }

        // Caso genérico para cualquier país con dialCode (ej: +57, +54, +58, +52, +51)
        return if (country.dialCode.isNotBlank()) "${country.dialCode}$digits" else "+$digits"
    }

    /**
     * Sanitiza una cadena telefónica o texto y verifica coincidencia con regex.
     * Elimina espacios y guiones en números telefónicos para que regexes con \d+ no fallen.
     */
    fun matchesRegexPattern(textOrNumber: String, regexPattern: String): Boolean {
        if (textOrNumber.isBlank() || regexPattern.isBlank()) return false
        return try {
            val pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            
            // Intento 1: Coincidencia con texto o número original
            if (pattern.matcher(textOrNumber).find()) return true

            // Intento 2: Sanitización de espacios y guiones en números telefónicos
            val sanitized = textOrNumber.replace(Regex("[\\s\\-\\(\\)]"), "")
            if (sanitized != textOrNumber && pattern.matcher(sanitized).find()) return true

            false
        } catch (e: Exception) {
            false
        }
    }

    data class DefaultRule(
        val pattern: String,
        val title: String,
        val category: String,
        val description: String,
        val countryCode: String = "CL" // "CL", "CO", "AR", "VE", "MX", "PE", "GLOBAL"
    )

    /**
     * Lista completa de reglas organizadas por país y globales LATAM.
     */
    fun getDefaultSpamRules(countryCode: String = "CL"): List<DefaultRule> {
        val globalRules = listOf(
            DefaultRule(
                pattern = "(?i)(7k|bet7k|joker jewels|fortune (ox|mouse|tiger|rabbit)|gates of olympus|sugar rush|apuestas|ruleta|giros|recarga.*5\\.000)",
                title = "SMS de Casinos y Apuestas Online",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Filtra publicidad no solicitada de tragamonedas, apuestas y casinos (7K, BET7K, Fortune Ox, etc.).",
                countryCode = "GLOBAL"
            ),
            DefaultRule(
                pattern = "(?i)(multa pendiente|aviso tag|copec|ultimo aviso|puntos disponibles|cuenta suspendida|paquete pendiente|regularice hoy)",
                title = "SMS de Falsas Multas, TAG y Cortes",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Captura SMS de cobros urgentes ficticios sobre autopistas, multas, entregas y puntos.",
                countryCode = "GLOBAL"
            ),
            DefaultRule(
                pattern = "(?i)(se han abonado|abonado.*pesos|compensacio?n|verifique su saldo|monto acreditado|transferencia recibida)",
                title = "SMS de Falsos Depósitos y Compensaciones",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Bloquea mensajes engañosos sobre abonos de dinero o devoluciones falsas.",
                countryCode = "GLOBAL"
            ),
            DefaultRule(
                pattern = "(?i)https?://(bit\\.ly|tinyurl\\.com|cutt\\.ly|is\\.gd|t\\.co|shorturl\\.at|([a-z0-9\\-]+\\.(xyz|top|site|club|ru|tk|online|fit|info|link|live|buzz)))",
                title = "SMS con Enlaces Acortados Sospechosos",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Intercepta SMS con links acortados usados para robar claves o infectar el equipo.",
                countryCode = "GLOBAL"
            ),
            DefaultRule(
                pattern = "^(\\+?34931|44\\d+|\\+?44\\d+)",
                title = "Troncales Robóticas VoIP / Spam Internacional",
                category = "🌐 Spam e Internacionales",
                description = "Filtra llamadas automatizadas y estafas desde centrales virtuales del exterior (+34 931, troncales 44).",
                countryCode = "GLOBAL"
            )
        )

        val countrySpecific = when (countryCode.uppercase()) {
            "CO" -> listOf(
                DefaultRule(
                    pattern = "^(\\+?57|0)?(018000|1800)\\d+",
                    title = "Líneas 018000 Comercial / Telemercadeo (Colombia)",
                    category = "📞 Llamadas Nacionales",
                    description = "Bloquea llamadas masivas de cobro y ventas telefónicas.",
                    countryCode = "CO"
                ),
                DefaultRule(
                    pattern = "^(\\+?57|0)?(300|301|310|320|601)\\d{7}$",
                    title = "Centrales Masivas Call Center (Colombia)",
                    category = "📞 Llamadas Nacionales",
                    description = "Intercepta llamadas no solicitadas desde centrales de Bogotá y móviles corporativos.",
                    countryCode = "CO"
                )
            )
            "AR" -> listOf(
                DefaultRule(
                    pattern = "^(\\+?54|0)?(0800|0810)\\d+",
                    title = "Líneas 0800 / 0810 Telemercadeo (Argentina)",
                    category = "📞 Llamadas Nacionales",
                    description = "Bloquea promociones masivas y centrales de venta automatizada.",
                    countryCode = "AR"
                ),
                DefaultRule(
                    pattern = "^(\\+?54|0)?11\\d{8}$",
                    title = "Fijos Telemarketing CABA / GBA (Argentina)",
                    category = "📞 Llamadas Nacionales",
                    description = "Filtra llamadas masivas no deseadas originadas en Buenos Aires.",
                    countryCode = "AR"
                )
            )
            "VE" -> listOf(
                DefaultRule(
                    pattern = "^(\\+?58|0)?(0800|0412|0414|0424)\\d{7}$",
                    title = "Líneas Masivas / Spam (Venezuela)",
                    category = "📞 Llamadas Nacionales",
                    description = "Bloquea llamadas automatizadas no solicitadas de telemercadeo.",
                    countryCode = "VE"
                )
            )
            "MX" -> listOf(
                DefaultRule(
                    pattern = "^(\\+?52|0)?(800|55)\\d{8}$",
                    title = "Líneas 800 y Call Centers CDMX (México)",
                    category = "📞 Llamadas Nacionales",
                    description = "Filtra spam telefónico de centrales automatizadas de ventas.",
                    countryCode = "MX"
                )
            )
            "PE" -> listOf(
                DefaultRule(
                    pattern = "^(\\+?51|0)?(0800|01)\\d{7,8}$",
                    title = "Líneas 0800 y Fijos Spam (Perú)",
                    category = "📞 Llamadas Nacionales",
                    description = "Intercepta marcadores automáticos de telemercadeo en Perú.",
                    countryCode = "PE"
                )
            )
            else -> listOf( // Por defecto Chile ("CL")
                DefaultRule(
                    pattern = "^(\\+?56|0)?(600|800|809|80)\\d+",
                    title = "Líneas 600 / 800 / 80 y Cobro Revertido",
                    category = "📞 Llamadas Nacionales",
                    description = "Bloquea llamadas masivas de venta comercial, cobranzas (80) y tarificación especial.",
                    countryCode = "CL"
                ),
                DefaultRule(
                    pattern = "^(\\+?56|0)?9(288\\d|443\\d|434\\d|523\\d|44\\d{2})\\d{4}$",
                    title = "Bloque Móvil Call Centers y Troncales SIP",
                    category = "📞 Llamadas Nacionales",
                    description = "Intercepta bloques de celulares corporativos (9 4343, 9 5233, 9 4434, 9 2882) de telemercadeo.",
                    countryCode = "CL"
                ),
                DefaultRule(
                    pattern = "^(\\+?56|0)?22\\d{7}$",
                    title = "Fijos de Telemarketing (Santiago)",
                    category = "📞 Llamadas Nacionales",
                    description = "Filtra llamadas de ventas no solicitadas desde números fijos de la RM.",
                    countryCode = "CL"
                ),
                DefaultRule(
                    pattern = "^(\\+?56|0)?(44|43|42|45|41)\\d+",
                    title = "Centrales VoIP y Números Comerciales",
                    category = "📞 Llamadas Nacionales",
                    description = "Bloquea llamadas salientes desde centrales telefónicas virtuales masivas.",
                    countryCode = "CL"
                )
            )
        }

        return countrySpecific + globalRules
    }

    /**
     * Mantiene compatibilidad de catálogo por defecto para Chile.
     */
    fun getDefaultChileSpamRules(): List<DefaultRule> {
        return getDefaultSpamRules("CL")
    }

    fun getDefaultChileSpamPatterns(): List<Pair<String, String>> {
        return getDefaultChileSpamRules().map { Pair(it.pattern, "${it.title}: ${it.description}") }
    }
}
