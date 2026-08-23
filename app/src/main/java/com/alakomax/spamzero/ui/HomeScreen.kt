package com.alakomax.spamzero.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.alakomax.spamzero.data.db.AppDatabase
import com.alakomax.spamzero.ui.components.MissingPermissionsDialog
import com.alakomax.spamzero.util.PermissionChecker
import com.alakomax.spamzero.util.ProtectionPreferences
import com.alakomax.spamzero.util.UpdateInfo
import com.alakomax.spamzero.util.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    onRequestRole: () -> Unit,
    onRequestSmsPermission: () -> Unit = {},
    onRequestNotificationListener: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var blockedCount by remember { mutableStateOf(0) }
    var rulesCount by remember { mutableStateOf(0) }

    var isRoleGranted by remember { mutableStateOf(PermissionChecker.isCallScreeningGranted(context)) }
    var isSmsPermissionGranted by remember { mutableStateOf(PermissionChecker.isSmsPermissionGranted(context)) }
    var isNotifListenerGranted by remember { mutableStateOf(PermissionChecker.isNotificationListenerGranted(context)) }
    var isBatteryOptimIgnored by remember { mutableStateOf(PermissionChecker.isBatteryOptimizationIgnored(context)) }
    var isAutostartDismissed by remember { mutableStateOf(ProtectionPreferences.isAutostartDismissed(context)) }

    val allSystemPermissionsGranted = isRoleGranted && isSmsPermissionGranted && isNotifListenerGranted && isBatteryOptimIgnored
    var isChecklistExpanded by remember { mutableStateOf(!allSystemPermissionsGranted) }

    var isProtectionEnabled by remember { mutableStateOf(ProtectionPreferences.isProtectionEnabled(context)) }
    var showMissingDialog by remember { mutableStateOf(false) }

    val currentVersion = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.1.3"
        } catch (e: Exception) {
            "1.1.3"
        }
    }

    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val refreshPermissionStates = {
        val role = PermissionChecker.isCallScreeningGranted(context)
        val sms = PermissionChecker.isSmsPermissionGranted(context)
        val notif = PermissionChecker.isNotificationListenerGranted(context)
        val battery = PermissionChecker.isBatteryOptimizationIgnored(context)
        isRoleGranted = role
        isSmsPermissionGranted = sms
        isNotifListenerGranted = notif
        isBatteryOptimIgnored = battery
        isAutostartDismissed = ProtectionPreferences.isAutostartDismissed(context)
        val allEssentialGranted = role && sms && notif
        val userPref = ProtectionPreferences.isProtectionEnabled(context)
        isProtectionEnabled = userPref && allEssentialGranted
    }

    LaunchedEffect(allSystemPermissionsGranted) {
        if (allSystemPermissionsGranted) {
            isChecklistExpanded = false
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshPermissionStates()
                scope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    val blocked = db.quarantineDao().getBlockedCount() + db.smsQuarantineDao().getSmsBlockedCount()
                    val rules = db.ruleDao().getRuleCount()
                    withContext(Dispatchers.Main) {
                        blockedCount = blocked
                        rulesCount = rules
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val checkUpdates = {
        isCheckingUpdate = true
        scope.launch {
            val result = UpdateManager.checkForUpdates(currentVersion)
            updateInfo = result
            isCheckingUpdate = false
        }
    }

    LaunchedEffect(Unit) {
        refreshPermissionStates()
        checkUpdates()
    }

    if (showMissingDialog) {
        MissingPermissionsDialog(
            isCallScreeningMissing = !isRoleGranted,
            isSmsMissing = !isSmsPermissionGranted,
            isNotifListenerMissing = !isNotifListenerGranted,
            onRequestRole = onRequestRole,
            onRequestSmsPermission = onRequestSmsPermission,
            onRequestNotificationListener = onRequestNotificationListener,
            onDismiss = { showMissingDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val allEssentialGranted = isRoleGranted && isSmsPermissionGranted && isNotifListenerGranted
        val cardColor = when {
            !allEssentialGranted -> Color(0xFF991B1B)
            isProtectionEnabled -> Color(0xFF1E3A8A)
            else -> Color(0xFFB45309)
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
                            !allEssentialGranted -> "Permisos Incompletos"
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
                            !allEssentialGranted -> "Faltan permisos esenciales. Revisa la lista inferior para activar el filtro."
                            isProtectionEnabled -> "El filtro previo a timbre y el interceptor SMS están funcionando 24/7."
                            else -> "El filtro está pausado. Las llamadas y SMS ingresarán normalmente."
                        },
                        color = Color(0xFFE2E8F0),
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Switch(
                        checked = isProtectionEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (PermissionChecker.areAllEssentialPermissionsGranted(context)) {
                                    isProtectionEnabled = true
                                    ProtectionPreferences.setProtectionEnabled(context, true)
                                } else {
                                    isProtectionEnabled = false
                                    ProtectionPreferences.setProtectionEnabled(context, false)
                                    showMissingDialog = true
                                }
                            } else {
                                isProtectionEnabled = false
                                ProtectionPreferences.setProtectionEnabled(context, false)
                            }
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
            }
        }

        // Card de Checklist Granular de Permisos (Desplegable)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val grantedCount = listOf(isRoleGranted, isSmsPermissionGranted, isNotifListenerGranted, isBatteryOptimIgnored).count { it }
                val totalCount = 4

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChecklistExpanded = !isChecklistExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Checklist de Permisos de Sistema",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = if (grantedCount == totalCount) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (grantedCount == totalCount) "✓ 4/4 Activos" else "$grantedCount/$totalCount Activos",
                                color = if (grantedCount == totalCount) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isChecklistExpanded = !isChecklistExpanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isChecklistExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isChecklistExpanded) "Minimizar checklist" else "Expandir checklist",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isChecklistExpanded) {
                    PermissionStatusRow(
                        title = "Filtro de Llamadas Predeterminado",
                        subtitle = "Requerido para silenciar llamadas en 0 repiques.",
                        isGranted = isRoleGranted,
                        icon = Icons.Default.PhoneInTalk,
                        onGrantClick = onRequestRole
                    )

                    PermissionStatusRow(
                        title = "Lectura e Interceptación de SMS",
                        subtitle = "Requerido para detectar estafas y apuestas.",
                        isGranted = isSmsPermissionGranted,
                        icon = Icons.AutoMirrored.Filled.Message,
                        onGrantClick = onRequestSmsPermission
                    )

                    PermissionStatusRow(
                        title = "Silenciado de Notificaciones SMS",
                        subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !isNotifListenerGranted)
                            "Si la opción sale gris: Ajustes > Aplicaciones > SpamZero > 3 puntos (⋮) > Permitir ajustes restringidos."
                        else
                            "Requerido para ocultar notificaciones de spam.",
                        isGranted = isNotifListenerGranted,
                        icon = Icons.Default.Notifications,
                        onGrantClick = onRequestNotificationListener
                    )

                    PermissionStatusRow(
                        title = "Sin Restricción de Batería",
                        subtitle = "Evita que Android cierre el filtro en segundo plano.",
                        isGranted = isBatteryOptimIgnored,
                        icon = Icons.Default.BatteryFull,
                        onGrantClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                runCatching {
                                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                        data = Uri.parse("package:${context.packageName}")
                                    }
                                    context.startActivity(intent)
                                }.onFailure {
                                    runCatching {
                                        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                            data = Uri.fromParts("package", context.packageName, null)
                                        }
                                        context.startActivity(fallbackIntent)
                                    }.onFailure {
                                        runCatching {
                                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                        }.onFailure {
                                            Toast.makeText(
                                                context,
                                                "Configura la batería en Sin Restricciones en Ajustes > Aplicaciones > SpamZero",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }

        // Tarjeta para Fabricantes Agresivos (Xiaomi / Huawei / Oppo / Vivo)
        if (PermissionChecker.isAggressiveBackgroundManufacturer() && !isAutostartDismissed) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF451A03)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Aviso para ${PermissionChecker.getManufacturerName()}",
                                color = Color(0xFFFDE68A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        IconButton(
                            onClick = {
                                ProtectionPreferences.setAutostartDismissed(context, true)
                                isAutostartDismissed = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Ocultar aviso",
                                tint = Color(0xFFFDE68A)
                            )
                        }
                    }
                    Text(
                        text = "Los teléfonos ${PermissionChecker.getManufacturerName()} cierran aplicaciones en segundo plano de forma agresiva. Para asegurar que SpamZero funcione 24/7, activa la opción 'Autoinicio'.",
                        color = Color(0xFFFEF3C7),
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = {
                            ProtectionPreferences.setAutostartDismissed(context, true)
                            isAutostartDismissed = true
                            val intent = PermissionChecker.getAutoStartIntent(context)
                            runCatching { context.startActivity(intent) }.onFailure {
                                Toast.makeText(context, "Abre Ajustes > Aplicaciones > SpamZero > Autoinicio", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Configurar Autoinicio en ${PermissionChecker.getManufacturerName()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
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
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=omargonzalez76@gmail.com&currency_code=USD")
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
private fun PermissionStatusRow(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) Color(0xFF10B981) else Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Concedido",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
        } else {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Activar", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
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
