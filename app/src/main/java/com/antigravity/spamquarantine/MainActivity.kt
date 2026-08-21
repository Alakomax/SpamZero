package com.antigravity.spamquarantine

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.antigravity.spamquarantine.ui.HomeScreen
import com.antigravity.spamquarantine.ui.LegalScreen
import com.antigravity.spamquarantine.ui.QuarantineScreen
import com.antigravity.spamquarantine.ui.RulesScreen
import com.antigravity.spamquarantine.ui.theme.SpamQuarantineTheme
import com.antigravity.spamquarantine.util.CountryUtils
import com.antigravity.spamquarantine.util.SpamRuleCache

class MainActivity : ComponentActivity() {

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "¡Filtro de llamadas activado correctamente!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Se requiere activar el permiso para interceptar llamadas spam.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-calentar el caché de reglas en RAM en segundo plano
        SpamRuleCache.prewarmCacheAsync(applicationContext)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val defaultDark = prefs.getBoolean("is_dark_mode", true)

        setContent {
            var isDarkMode by remember { mutableStateOf(defaultDark) }

            SpamQuarantineTheme(darkTheme = isDarkMode) {
                MainAppStructure(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = {
                        val newMode = !isDarkMode
                        isDarkMode = newMode
                        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
                    },
                    onRequestRole = { requestCallScreeningRole() }
                )
            }
        }
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            } else {
                Toast.makeText(this, "El filtro de llamadas ya se encuentra activo.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Su versión de Android gestiona el permiso desde los ajustes del sistema.", Toast.LENGTH_LONG).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onRequestRole: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val countryInfo = remember { CountryUtils.getSimCountryInfo(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SpamZero ${countryInfo.flagEmoji}",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { selectedTab = 3 }) {
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = "Información Legal"
                        )
                    }
                    IconButton(onClick = onToggleDarkMode) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Block, contentDescription = "Cuarentena") },
                    label = { Text("Cuarentena") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Reglas") },
                    label = { Text("Reglas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Gavel, contentDescription = "Legal") },
                    label = { Text("Legal") }
                )
            }
        }
    ) { paddingValues ->
        Surface(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(onRequestRole = onRequestRole)
                1 -> QuarantineScreen()
                2 -> RulesScreen()
                3 -> LegalScreen()
            }
        }
    }
}
