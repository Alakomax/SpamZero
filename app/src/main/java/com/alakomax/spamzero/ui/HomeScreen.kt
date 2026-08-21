package com.alakomax.spamzero.ui

import android.app.role.RoleManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
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
import com.alakomax.spamzero.util.ProtectionPreferences
import com.alakomax.spamzero.util.UpdateInfo
import com.alakomax.spamzero.util.UpdateManager
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onRequestRole: () -> Unit) {
    val context = LocalContext.current
    var blockedCount by remember { mutableStateOf(0) }
    var rulesCount by remember { mutableStateOf(0) }
    var isRoleGranted by remember { mutableStateOf(checkRoleGranted(context)) }
    var isProtectionEnabled by remember { mutableStateOf(ProtectionPreferences.isProtectionEnabled(context)) }

    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1.0"
        } catch (e: Exception) {
            "1.1.0"
        }
    }

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val checkUpdates = {
        isCheckingUpdate = true
        scope.launch {
            val result = UpdateManager.checkForUpdates(currentVersion)
            updateInfo = result
            isCheckingUpdate = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            val db = AppDatabase.getDatabase(context)
            blockedCount = db.quarantineDao().getBlockedCount() + db.smsQuarantineDao().getSmsBlockedCount()
            rulesCount = db.ruleDao().getRuleCount()
            isRoleGranted = checkRoleGranted(context)
            isProtectionEnabled = ProtectionPreferences.isProtectionEnabled(context)
        }
        checkUpdates()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tarjeta de Estado del Filtro
        val cardColor = when {
            !isRoleGranted -> Color(0xFF991B1B) // Rojo: Sin permiso
            isProtectionEnabled -> Color(0xFF1E3A8A) // Azul: Protección activa
            else -> Color(0xFFB45309) // Naranja: Pausada/Desactivada
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when {
                            !isRoleGranted -> "Permiso Inactivo"
                            isProtectionEnabled -> "Protección Activa"
                            else -> "Protección Pausada"
                        },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            !isRoleGranted -> "Debes otorgar el rol de Filtro de llamadas para bloquear automáticamente."
                            isProtectionEnabled -> "El filtro previo a timbre está interceptando números spam en 0 repiques."
                            else -> "El filtro automático está desactivado. Las llamadas entrantes ingresarán normalmente."
                        },
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (isRoleGranted) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = isProtectionEnabled,
                            onCheckedChange = { enabled ->
                                isProtectionEnabled = enabled
                                ProtectionPreferences.setProtectionEnabled(context, enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF4B5563)
                            )
                        )
                        Text(
                            text = if (isProtectionEnabled) "ON" else "OFF",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        if (!isRoleGranted) {
            Button(
                onClick = onRequestRole,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Activar Filtro de Llamadas Predeterminado", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        // Tarjeta de Actualizaciones In-App
        val info = updateInfo
        if (info != null && info.hasUpdate) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF065F46)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Nueva versión ${info.latestVersionName} disponible!",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Versión actual instalada: v$currentVersion",
                                color = Color(0xFFA7F3D0),
                                fontSize = 12.sp
                            )
                        }
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isDownloading) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Descargando actualización: $downloadProgress%", color = Color.White, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF34D399),
                                trackColor = Color(0xFF047857)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isDownloading = true
                                scope.launch {
                                    UpdateManager.downloadAndInstallApk(
                                        context = context,
                                        downloadUrl = info.downloadUrl,
                                        onProgress = { downloadProgress = it },
                                        onError = { errorMsg ->
                                            isDownloading = false
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Actualizar ahora sin desinstalar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Versión de la app: v$currentVersion", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = if (isCheckingUpdate) "Verificando en GitHub..." else "La app está actualizada.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { checkUpdates() },
                        enabled = !isCheckingUpdate
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Buscar actualizaciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Métricas rápidas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Bloqueadas en Cuarentena",
                value = blockedCount.toString(),
                icon = Icons.Default.Block,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Reglas de Patrones",
                value = rulesCount.toString(),
                icon = Icons.Default.Shield,
                modifier = Modifier.weight(1f)
            )
        }

        // Tarjeta "Invítame un café ☕"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invítame un café ☕",
                        color = Color(0xFFF59E0B),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Apoya el desarrollo de la app con una donación en PayPal.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=omargonzalez76@gmail.com&currency_code=USD")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Donar ☕", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Column {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

private fun checkRoleGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
        return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) ?: false
    }
    return true
}
