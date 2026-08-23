package com.alakomax.spamzero.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MissingPermissionsDialog(
    isCallScreeningMissing: Boolean,
    isSmsMissing: Boolean,
    isNotifListenerMissing: Boolean,
    onRequestRole: () -> Unit,
    onRequestSmsPermission: () -> Unit,
    onRequestNotificationListener: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Se requieren permisos para activar la protección",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Para habilitar el filtro en tiempo real, debes conceder los siguientes permisos en tu dispositivo:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isCallScreeningMissing) {
                    MissingItemRow(
                        title = "Filtro de llamadas predeterminado",
                        description = "Necesario para cortar llamadas spam antes de sonar.",
                        buttonText = "Conceder Rol",
                        buttonColor = Color(0xFF2563EB),
                        onClick = {
                            onRequestRole()
                            onDismiss()
                        }
                    )
                }

                if (isSmsMissing) {
                    MissingItemRow(
                        title = "Lectura de mensajes SMS",
                        description = "Requerido para analizar remitentes y enlaces sospechosos.",
                        buttonText = "Permitir SMS",
                        buttonColor = Color(0xFF7C3AED),
                        onClick = {
                            onRequestSmsPermission()
                            onDismiss()
                        }
                    )
                }

                if (isNotifListenerMissing) {
                    MissingItemRow(
                        title = "Silenciado de notificaciones",
                        description = "Necesario para ocultar alertas de SMS spam en segundo plano.",
                        buttonText = "Activar Escucha",
                        buttonColor = Color(0xFF059669),
                        onClick = {
                            onRequestNotificationListener()
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun MissingItemRow(
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text(text = buttonText, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
