package com.alakomax.spamzero.util

import android.content.Context
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.RuleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object SpamRuleCache {

    @Volatile
    private var cachedRules: List<RuleEntity>? = null

    private val cacheScope = CoroutineScope(Dispatchers.IO)

    /**
     * Pre-calienta el caché de reglas en la memoria RAM en segundo plano sin bloquear la UI ni llamadas entrantes.
     */
    fun prewarmCacheAsync(context: Context) {
        cacheScope.launch {
            try {
                getActiveRulesSync(context.applicationContext)
            } catch (e: Exception) {
                // Silencioso
            }
        }
    }

    /**
     * Obtiene la lista de reglas activas en memoria de manera ultra-rápida y síncrona.
     * Evita consultas a disco durante la llegada de una llamada entrante.
     */
    fun getActiveRulesSync(context: Context): List<RuleEntity> {
        val current = cachedRules
        if (current != null) {
            return current
        }

        return synchronized(this) {
            cachedRules ?: runBlocking(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val existingRules = db.ruleDao().getAllRules()
                    val existingPatterns = existingRules.map { it.pattern }.toSet()

                    val countryInfo = CountryUtils.getSimCountryInfo(context.applicationContext)
                    val defaultRules = PhoneUtils.getDefaultSpamRules(countryInfo.code)

                    var insertedAny = false
                    defaultRules.forEach { rule ->
                        if (!existingPatterns.contains(rule.pattern)) {
                            db.ruleDao().insertRule(
                                RuleEntity(
                                    pattern = rule.pattern,
                                    title = rule.title,
                                    category = rule.category,
                                    description = rule.description
                                )
                            )
                            insertedAny = true
                        }
                    }

                    val rules = if (insertedAny) db.ruleDao().getActiveRules() else existingRules.filter { it.isActive }
                    cachedRules = rules
                    rules
                } catch (e: Exception) {
                    val countryInfo = CountryUtils.getSimCountryInfo(context.applicationContext)
                    PhoneUtils.getDefaultSpamRules(countryInfo.code).map { rule ->
                        RuleEntity(
                            pattern = rule.pattern,
                            title = rule.title,
                            category = rule.category,
                            description = rule.description
                        )
                    }.also { cachedRules = it }
                }
            }
        }
    }

    /**
     * Sincroniza/restaura las reglas por defecto para un país específico o el de la SIM.
     */
    fun restoreDefaultRulesSync(context: Context, countryCode: String? = null): List<RuleEntity> {
        return synchronized(this) {
            runBlocking(Dispatchers.IO) {
                try {
                    val targetCountryIso = countryCode ?: CountryUtils.getSimCountryInfo(context.applicationContext).code
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val existingRules = db.ruleDao().getAllRules()

                    val targetRules = PhoneUtils.getDefaultSpamRules(targetCountryIso)
                    val validPatterns = targetRules.map { it.pattern }.toSet()
                    val allKnownDefaultPatterns = PhoneUtils.getAllKnownDefaultPatterns()

                    // Eliminar reglas por defecto pertenecientes a otros países
                    existingRules.forEach { existing ->
                        if (allKnownDefaultPatterns.contains(existing.pattern) && !validPatterns.contains(existing.pattern)) {
                            db.ruleDao().deleteRule(existing)
                        }
                    }

                    val updatedExistingPatterns = db.ruleDao().getAllRules().map { it.pattern }.toSet()

                    // Insertar reglas faltantes del país activo
                    targetRules.forEach { rule ->
                        if (!updatedExistingPatterns.contains(rule.pattern)) {
                            db.ruleDao().insertRule(
                                RuleEntity(
                                    pattern = rule.pattern,
                                    title = rule.title,
                                    category = rule.category,
                                    description = rule.description
                                )
                            )
                        }
                    }

                    val rules = db.ruleDao().getAllRules()
                    cachedRules = rules.filter { it.isActive }
                    rules
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
    }

    /**
     * Invalida el caché en memoria para que se recargue la próxima vez desde la BD.
     */
    fun invalidateCache() {
        cachedRules = null
    }

    /**
     * Actualiza directamente la lista de reglas activas en el caché.
     */
    fun updateCache(rules: List<RuleEntity>) {
        cachedRules = rules.filter { it.isActive }
    }
}
