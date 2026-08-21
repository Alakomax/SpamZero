package com.alakomax.spamzero.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.RuleEntity
import com.alakomax.spamzero.util.CountryUtils
import com.alakomax.spamzero.util.SpamRuleCache
import kotlinx.coroutines.launch

@Composable
fun RulesScreen() {
    val context = LocalContext.current
    var rules by remember { mutableStateOf<List<RuleEntity>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val simCountry = remember { CountryUtils.getSimCountryInfo(context) }
    var selectedCountry by remember { mutableStateOf(simCountry) }

    var newTitle by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("📞 Llamadas Nacionales") }
    var newDescription by remember { mutableStateOf("") }
    var newPattern by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val loadRules = {
        scope.launch {
            SpamRuleCache.getActiveRulesSync(context)
            val db = AppDatabase.getDatabase(context)
            rules = db.ruleDao().getAllRules()
        }
    }

    LaunchedEffect(Unit) {
        loadRules()
    }

    val groupedRules = remember(rules) {
        rules.groupBy { if (it.category.isNotBlank()) it.category else "General" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Cabecera principal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Reglas de Protección", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Motores de Llamadas y SMS Spam LATAM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row {
                IconButton(
                    onClick = {
                        scope.launch {
                            SpamRuleCache.restoreDefaultRulesSync(context, selectedCountry.code)
                            loadRules()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Restaurar Reglas Predeterminadas", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Regla", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tarjeta de Información de País y SIM
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCountryPicker = true },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedCountry.flagEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "País Activo: ${selectedCountry.name} (${selectedCountry.dialCode.ifEmpty { "Global" }})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (selectedCountry.code == simCountry.code) "Detección Automática por SIM" else "Selección Manual de Reglas",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = "Cambiar País",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            groupedRules.forEach { (categoryName, categoryRules) ->
                item(key = "header_$categoryName") {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                items(categoryRules, key = { it.id }) { rule ->
                    RuleItem(rule = rule, onDelete = {
                        scope.launch {
                            AppDatabase.getDatabase(context).ruleDao().deleteRule(rule)
                            loadRules()
                        }
                    })
                }
            }
        }
    }

    // Modal para seleccionar País LATAM
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = { Text("Seleccionar País (LATAM)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CountryUtils.SUPPORTED_COUNTRIES.forEach { country ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = country
                                    showCountryPicker = false
                                    scope.launch {
                                        SpamRuleCache.restoreDefaultRulesSync(context, country.code)
                                        loadRules()
                                    }
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedCountry.code == country.code) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = country.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${country.name} (${country.dialCode})",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPicker = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Nueva Regla de Bloqueo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Título Informativo") },
                        placeholder = { Text("Filtro SMS Banco X") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Significado / Explicación") },
                        placeholder = { Text("Bloquea SMS de cobros ficticios") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        label = { Text("Expresión Regex / Patrón") },
                        placeholder = { Text("^\\+5692882\\d{4}$") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategory,
                        onValueChange = { newCategory = it },
                        label = { Text("Categoría") },
                        placeholder = { Text("📩 SMS y Estafas (Phishing)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newPattern.isNotBlank()) {
                        scope.launch {
                            AppDatabase.getDatabase(context).ruleDao().insertRule(
                                RuleEntity(
                                    pattern = newPattern.trim(),
                                    title = newTitle.trim(),
                                    category = if (newCategory.isNotBlank()) newCategory.trim() else "General",
                                    description = newDescription.trim()
                                )
                            )
                            newTitle = ""
                            newDescription = ""
                            newPattern = ""
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val displayTitle = if (rule.title.isNotBlank()) rule.title else rule.description
                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (rule.title.isNotBlank() && rule.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = rule.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Patrón: ${rule.pattern}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
