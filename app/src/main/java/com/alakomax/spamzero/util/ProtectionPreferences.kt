package com.alakomax.spamzero.util

import android.content.Context
import android.content.SharedPreferences

object ProtectionPreferences {
    private const val PREFS_NAME = "app_settings"
    private const val KEY_PROTECTION_ENABLED = "is_protection_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Devuelve si la protección automática de llamadas spam está activada.
     * Por defecto es true.
     */
    fun isProtectionEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PROTECTION_ENABLED, true)
    }

    /**
     * Guarda el estado de la protección (activado/desactivado).
     */
    fun setProtectionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }
}
