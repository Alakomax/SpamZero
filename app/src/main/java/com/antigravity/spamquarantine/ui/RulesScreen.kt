package com.antigravity.spamquarantine.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antigravity.spamquarantine.data.db.AppDatabase
import com.antigravity.spamquarantine.data.model.RuleEntity
import com.antigravity.spamquarantine.util.PhoneUtils
import com.antigravity.spamquarantine.util.SpamRuleCache
import kotlinx.coroutines.launch

@Composable
fun RulesScreen() {
    val context = LocalContext.current
    var rules by remember { mutableStateOf<List<RuleEntity>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var newPattern by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val loadRules = {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            var currentRules = db.ruleDao().getAllRules()
            if (currentRules.isEmpty()) {
                // Precargar las reglas predeterminadas para Chile
                PhoneUtils.getDefaultChileSpamPatterns().forEach { (pattern, desc) ->
                    db.ruleDao().insertRule(RuleEntity(pattern = pattern, description = desc))
                }
                currentRules = db.ruleDao().getAllRules()
            }
            rules = currentRules
            SpamRuleCache.updateCache(currentRules)
        }
    }

    LaunchedEffect(Unit) {
        loadRules()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reglas de Patrones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = { showDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Regla", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rules) { rule ->
                RuleItem(rule = rule, onDelete = {
                    scope.launch {
                        AppDatabase.getDatabase(context).ruleDao().deleteRule(rule)
                        loadRules()
                    }
                })
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Regla de Bloqueo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        label = { Text("Expresión Regex / Patrón") },
                        placeholder = { Text("^\\+5692882\\d{4}$") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Descripción") },
                        placeholder = { Text("Rango Call Center Movistar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Ejemplo Regex para Chile:\n• Prefijo 600/800: ^\\+56(600|800)\\d+\n• Rango Móvil: ^\\+5692882\\d{4}$",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPattern.isNotBlank()) {
                        scope.launch {
                            AppDatabase.getDatabase(context).ruleDao().insertRule(
                                RuleEntity(pattern = newPattern.trim(), description = newDescription.trim())
                            )
                            newPattern = ""
                            newDescription = ""
                            showDialog = false
                            loadRules()
                        }
                    }
                }) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun RuleItem(rule: RuleEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = rule.description, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = rule.pattern, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}
