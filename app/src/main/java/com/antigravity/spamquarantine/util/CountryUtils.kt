package com.antigravity.spamquarantine.util

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object CountryUtils {

    data class CountryInfo(
        val code: String,       // p.ej. "CL", "CO", "AR", "VE", "MX", "PE"
        val name: String,       // p.ej. "Chile"
        val flagEmoji: String,  // p.ej. "🇨🇱"
        val dialCode: String    // p.ej. "+56"
    )

    val SUPPORTED_COUNTRIES = listOf(
        CountryInfo("CL", "Chile", "🇨🇱", "+56"),
        CountryInfo("CO", "Colombia", "🇨🇴", "+57"),
        CountryInfo("AR", "Argentina", "🇦🇷", "+54"),
        CountryInfo("VE", "Venezuela", "🇻🇪", "+58"),
        CountryInfo("MX", "México", "🇲🇽", "+52"),
        CountryInfo("PE", "Perú", "🇵🇪", "+51")
    )

    private val DEFAULT_COUNTRY = CountryInfo("CL", "Chile", "🇨🇱", "+56")

    /**
     * Obtiene la información del país de la SIM activa del dispositivo.
     */
    fun getSimCountryInfo(context: Context): CountryInfo {
        return try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simCountryIso = tm?.simCountryIso?.uppercase(Locale.ROOT)?.trim() ?: ""

            if (simCountryIso.isNotBlank()) {
                val found = SUPPORTED_COUNTRIES.find { it.code == simCountryIso }
                if (found != null) return found

                // Si es un país con ISO de 2 letras válido no listado expresamente, generar bandera dinámica
                if (simCountryIso.length == 2 && simCountryIso.all { it.isLetter() }) {
                    return CountryInfo(
                        code = simCountryIso,
                        name = Locale("", simCountryIso).displayCountry,
                        flagEmoji = getFlagEmoji(simCountryIso),
                        dialCode = ""
                    )
                }
            }

            // Fallback por defecto: Chile 🇨🇱
            DEFAULT_COUNTRY
        } catch (e: Exception) {
            DEFAULT_COUNTRY
        }
    }

    /**
     * Convierte un código ISO de 2 letras (ej: "CL") a un Emoji de Bandera (ej: "🇨🇱").
     */
    fun getFlagEmoji(countryCode: String): String {
        if (countryCode.length != 2) return "🌎"
        val uppercaseCode = countryCode.uppercase(Locale.ROOT)
        val firstChar = Character.codePointAt(uppercaseCode, 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(uppercaseCode, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }

    fun getCountryByCode(code: String): CountryInfo {
        return SUPPORTED_COUNTRIES.find { it.code.equals(code, ignoreCase = true) } ?: DEFAULT_COUNTRY
    }
}
