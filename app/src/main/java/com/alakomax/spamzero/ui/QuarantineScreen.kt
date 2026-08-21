package com.alakomax.spamzero.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.PhoneMissed
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.data.model.QuarantineLogEntity
import com.alakomax.spamzero.data.model.SmsQuarantineLogEntity
import com.alakomax.spamzero.util.QuarantineExporter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen() {
    val context = LocalContext.current
    var callLogs by remember { mutableStateOf<List<QuarantineLogEntity>>(emptyList()) }
    var smsLogs by remember { mutableStateOf<List<SmsQuarantineLogEntity>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val loadAllLogs = {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            callLogs = db.quarantineDao().getAllQuarantineLogs()
            smsLogs = db.smsQuarantineDao().getAllSmsLogs()
        }
    }

    LaunchedEffect(Unit) {
        loadAllLogs()
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
            Text(
                text = "Historial en Cuarentena",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row {
                if (callLogs.isNotEmpty() || smsLogs.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            QuarantineExporter.exportAndShareReport(context, callLogs, smsLogs)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Exportar Informe Probatorio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {
                            scope.launch {
                                val db = AppDatabase.getDatabase(context)
                                if (selectedTab == 0) {
                                    db.quarantineDao().clearAll()
                                } else {
                                    db.smsQuarantineDao().clearAllSms()
                                }
                                loadAllLogs()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Limpiar Todo",
                            tint = Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Llamadas (${callLogs.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Phone, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Mensajes SMS (${smsLogs.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            if (callLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay llamadas en cuarentena.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(callLogs) { log ->
                        QuarantineItem(log = log, onDelete = {
                            scope.launch {
                                AppDatabase.getDatabase(context).quarantineDao().deleteLog(log)
                                loadAllLogs()
                            }
                        })
                    }
                }
            }
        } else {
            if (smsLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay mensajes SMS en cuarentena.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(smsLogs) { smsLog ->
                        SmsQuarantineItem(smsLog = smsLog, onDelete = {
                            scope.launch {
                                AppDatabase.getDatabase(context).smsQuarantineDao().deleteSmsLog(smsLog)
                                loadAllLogs()
                            }
                        })
                    }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
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

@Composable
fun SmsQuarantineItem(smsLog: SmsQuarantineLogEntity, onDelete: () -> Unit) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val dateStr = dateFormat.format(Date(smsLog.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Comment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = smsLog.senderPhoneNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = smsLog.messageBody,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Patrón: ${smsLog.matchedPattern}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
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
