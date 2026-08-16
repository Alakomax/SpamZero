package com.antigravity.spamquarantine.util

import android.content.Context
import com.antigravity.spamquarantine.data.db.AppDatabase
import com.antigravity.spamquarantine.data.model.RuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object SpamRuleCache {

    @Volatile
    private var cachedRules: List<RuleEntity>? = null

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

                    var insertedAny = false
                    PhoneUtils.getDefaultChileSpamRules().forEach { rule ->
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
                    PhoneUtils.getDefaultChileSpamRules().map { rule ->
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
     * Fuerza la restauración/reinicio de todas las reglas por defecto en la base de datos.
     */
    fun restoreDefaultRulesSync(context: Context): List<RuleEntity> {
        return synchronized(this) {
            runBlocking(Dispatchers.IO) {
                try {
                    val db = AppDatabase.getDatabase(context.applicationContext)
                    val existingRules = db.ruleDao().getAllRules()
                    val existingPatterns = existingRules.map { it.pattern }.toSet()

                    PhoneUtils.getDefaultChileSpamRules().forEach { rule ->
                        if (!existingPatterns.contains(rule.pattern)) {
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
