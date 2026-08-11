package com.antigravity.spamquarantine.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Información Legal y Privacidad",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Privacidad", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Términos", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Gavel, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Licencias", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Info, contentDescription = null) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (selectedTab) {
                0 -> PrivacySection()
                1 -> TermsSection()
                2 -> LicensesSection()
            }
        }
    }
}

@Composable
fun PrivacySection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegalCard(
            title = "100% Local (0 Servidores)",
            icon = Icons.Default.Shield,
            description = "SpamQuarantine no posee servidores propios ni envía datos a la nube. Todo el procesamiento de números y base de datos funciona de forma síncrona en tu procesador."
        )

        LegalCard(
            title = "Sin Cookies ni Rastreadores",
            icon = Icons.Default.Cookie,
            description = "Esta aplicación nativa no utiliza cookies de navegación, analíticas ni herramientas de rastreo. No recopilamos telemetría ni hábitos de uso."
        )

        LegalCard(
            title = "Uso de Permisos",
            icon = Icons.Default.Lock,
            description = "• CallScreeningService: Requerido para cortar llamadas spam antes del primer timbre.\n• Leer Contactos: Usado exclusivamente para la Lista Blanca local. Tu agenda jamás se copia ni se envía."
        )
    }
}

@Composable
fun TermsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegalCard(
            title = "Condiciones de Uso",
            icon = Icons.Default.Gavel,
            description = "La aplicación se entrega 'tal cual' (As Is) bajo Licencia MIT. El usuario puede configurar o modificar las reglas Regex bajo su propia discreción."
        )

        LegalCard(
            title = "Deslinde de Responsabilidad",
            icon = Icons.Default.Info,
            description = "Aunque la app incluye reglas optimizadas para números comerciales en Chile (600, 800, 809), no se garantiza la interceptación del 100% de spammers ni nos responsabilizamos por llamadas rechazadas por reglas configuradas por el usuario."
        )
    }
}

@Composable
fun LicensesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegalCard(
            title = "Licencia de Código Abierto (MIT)",
            icon = Icons.Default.Info,
            description = "Copyright (c) 2026 Alakomax. Licencia libre para usar, copiar, modificar y redistribuir el software."
        )

        LegalCard(
            title = "Componentes de Terceros",
            icon = Icons.Default.Shield,
            description = "Desarrollado con AndroidX, Kotlin, Jetpack Compose, Room Database y Material Design Icons bajo Licencia Apache 2.0."
        )
    }
}

@Composable
fun LegalCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f), lineHeight = 18.sp)
        }
    }
}
