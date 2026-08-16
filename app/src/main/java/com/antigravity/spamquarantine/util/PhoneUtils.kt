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
     * Incluye Pattern.DOTALL para soportar la evaluación de mensajes SMS multilínea.
     */
    fun matchesRegexPattern(textOrNumber: String, regexPattern: String): Boolean {
        if (textOrNumber.isBlank() || regexPattern.isBlank()) return false
        return try {
            val pattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
            pattern.matcher(textOrNumber).find()
        } catch (e: Exception) {
            false
        }
    }

    data class DefaultRule(
        val pattern: String,
        val title: String,
        val category: String,
        val description: String
    )

    /**
     * Lista de reglas predeterminadas organizadas por categoría y con explicaciones concisas.
     */
    fun getDefaultChileSpamRules(): List<DefaultRule> {
        return listOf(
            DefaultRule(
                pattern = "^\\+56(600|800|809)\\d+",
                title = "Líneas 600 / 800 y Cobro Revertido",
                category = "📞 Llamadas Nacionales",
                description = "Bloquea llamadas masivas de venta comercial y tarificación especial."
            ),
            DefaultRule(
                pattern = "^\\+569(2882|4434|4433|4435)\\d{4}$",
                title = "Bloque Móvil Call Centers",
                category = "📞 Llamadas Nacionales",
                description = "Intercepta bloques de celulares corporativos contratados por centrales de telemercadeo."
            ),
            DefaultRule(
                pattern = "^\\+5680\\d+",
                title = "Sistemas de Cobranza Automática",
                category = "📞 Llamadas Nacionales",
                description = "Bloquea números configurados por agencias de cobranza masiva."
            ),
            DefaultRule(
                pattern = "^\\+5622\\d{7}$",
                title = "Fijos de Telemarketing (Santiago)",
                category = "📞 Llamadas Nacionales",
                description = "Filtra llamadas de ventas no solicitadas desde números fijos de la RM."
            ),
            DefaultRule(
                pattern = "^\\+56(44|43|42|45|41)\\d+",
                title = "Centrales VoIP y Números Comerciales",
                category = "📞 Llamadas Nacionales",
                description = "Bloquea llamadas salientes desde centrales telefónicas virtuales masivas."
            ),
            DefaultRule(
                pattern = "^\\+(?!56)\\d{8,}$",
                title = "Remitentes Internacionales No Guardados",
                category = "🌐 Spam e Internacionales",
                description = "Bloquea SMS y llamadas entrantes de números extranjeros fuera de tu agenda."
            ),
            DefaultRule(
                pattern = "^\\+?34931\\d+$",
                title = "Troncales Robóticas España / VoIP",
                category = "🌐 Spam e Internacionales",
                description = "Filtra llamadas automatizadas y estafas desde centrales virtuales del exterior (+34 931)."
            ),
            DefaultRule(
                pattern = "^44\\d+$",
                title = "Troncales VoIP Estafas SMS",
                category = "🌐 Spam e Internacionales",
                description = "Intercepta números virtuales internacionales usados para envío masivo de spam."
            ),
            DefaultRule(
                pattern = "(?i)(se han abonado|abonado.*pesos|compensacio?n|verifique su saldo|monto acreditado|transferencia recibida)",
                title = "SMS de Falsos Depósitos y Compensaciones",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Bloquea mensajes engañosos sobre abonos de dinero o devoluciones falsas."
            ),
            DefaultRule(
                pattern = "(?i)(multa pendiente|aviso tag|copec|ultimo aviso|puntos disponibles|cuenta suspendida)",
                title = "SMS de Falsas Multas, TAG y Cortes",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Captura SMS de cobros urgentes ficticios sobre autopistas, multas y puntos."
            ),
            DefaultRule(
                pattern = "(?i)(joker jewels|fortune (tiger|rabbit)|gates of olympus|7k|apuestas|ruleta|giros)",
                title = "SMS de Casinos y Apuestas Online",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Filtra publicidad no solicitada de tragamonedas y juegos de azar."
            ),
            DefaultRule(
                pattern = "(?i)https?://(bit\\.ly|tinyurl\\.com|cutt\\.ly|is\\.gd|t\\.co|shorturl\\.at|([a-z0-9\\-]+\\.(xyz|top|site|club|ru|tk|online|fit|info|link|live|buzz)))",
                title = "SMS con Enlaces Acortados (bit.ly)",
                category = "📩 SMS y Estafas (Phishing)",
                description = "Intercepta SMS con links acortados usados para robar claves o infectar el equipo."
            )
        )
    }

    /**
     * Mantiene compatibilidad hacia atrás devolviendo la lista de patrones como Pares.
     */
    fun getDefaultChileSpamPatterns(): List<Pair<String, String>> {
        return getDefaultChileSpamRules().map { Pair(it.pattern, "${it.title}: ${it.description}") }
    }
}
