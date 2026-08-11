package com.antigravity.spamquarantine.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
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
import com.antigravity.spamquarantine.data.model.QuarantineLogEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QuarantineScreen() {
    val context = LocalContext.current
    var logs by remember { mutableStateOf<List<QuarantineLogEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val loadLogs = {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            logs = db.quarantineDao().getAllQuarantineLogs()
        }
    }

    LaunchedEffect(Unit) {
        loadLogs()
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
            Text("Llamadas en Cuarentena", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            if (logs.isNotEmpty()) {
                IconButton(onClick = {
                    scope.launch {
                        AppDatabase.getDatabase(context).quarantineDao().clearAll()
                        loadLogs()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Limpiar Todo", tint = Color(0xFFEF4444))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay llamadas en cuarentena actualmente.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(logs) { log ->
                    QuarantineItem(log = log, onDelete = {
                        scope.launch {
                            AppDatabase.getDatabase(context).quarantineDao().deleteLog(log)
                            loadLogs()
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun QuarantineItem(log: QuarantineLogEntity, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(log.timestamp))

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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PhoneMissed,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = log.normalizedPhoneNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Patrón: ${log.matchedPattern}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}
