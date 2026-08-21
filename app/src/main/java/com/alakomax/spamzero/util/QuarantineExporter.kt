package com.alakomax.spamzero.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.alakomax.spamzero.data.model.QuarantineLogEntity
import com.alakomax.spamzero.data.model.SmsQuarantineLogEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object QuarantineExporter {

    /**
     * Genera un reporte probatorio estructurado (.txt) con todos los eventos de llamadas y SMS bloqueados,
     * y abre la interfaz del sistema para guardar o compartir el documento.
     */
    fun exportAndShareReport(
        context: Context,
        callLogs: List<QuarantineLogEntity>,
        smsLogs: List<SmsQuarantineLogEntity>
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val sb = StringBuilder()
        sb.appendLine("====================================================================")
        sb.appendLine("INFORME DE EVIDENCIA AUDITADA DE SPAM Y ACOSO TELEFÓNICO")
        sb.appendLine("Generado por SpamZero | Dispositivo Android Nativo")
        sb.appendLine("Fecha de Emisión: ${dateFormat.format(Date())}")
        sb.appendLine("====================================================================")
        sb.appendLine()
        sb.appendLine("RESUMEN DE EVIDENCIAS ACUMULADAS:")
        sb.appendLine("• Llamadas Bloqueadas: ${callLogs.size}")
        sb.appendLine("• Mensajes SMS Interceptados: ${smsLogs.size}")
        sb.appendLine("• Total Registros: ${callLogs.size + smsLogs.size}")
        sb.appendLine()
        sb.appendLine("--------------------------------------------------------------------")
        sb.appendLine("1. HISTORIAL DE LLAMADAS TELEFÓNICAS BLOQUEADAS")
        sb.appendLine("--------------------------------------------------------------------")

        if (callLogs.isEmpty()) {
            sb.appendLine("Sin llamadas registradas en la base de datos de cuarentena.")
        } else {
            callLogs.forEachIndexed { index, log ->
                sb.appendLine("[Llamada #${index + 1}]")
                sb.appendLine("Remitente Raw: ${log.rawPhoneNumber}")
                sb.appendLine("Formato E.164: ${log.normalizedPhoneNumber}")
                sb.appendLine("Fecha y Hora: ${dateFormat.format(Date(log.timestamp))}")
                sb.appendLine("Patrón Coincidente: ${log.matchedPattern}")
                sb.appendLine("---")
            }
        }

        sb.appendLine()
        sb.appendLine("--------------------------------------------------------------------")
        sb.appendLine("2. HISTORIAL DE MENSAJES SMS INTERCEPTADOS")
        sb.appendLine("--------------------------------------------------------------------")

        if (smsLogs.isEmpty()) {
            sb.appendLine("Sin mensajes SMS registrados en la base de datos de cuarentena.")
        } else {
            smsLogs.forEachIndexed { index, log ->
                sb.appendLine("[SMS #${index + 1}]")
                sb.appendLine("Remitente: ${log.senderPhoneNumber}")
                sb.appendLine("Fecha y Hora: ${dateFormat.format(Date(log.timestamp))}")
                sb.appendLine("Patrón Coincidente: ${log.matchedPattern}")
                sb.appendLine("Contenido Mensaje: ${log.messageBody}")
                sb.appendLine("---")
            }
        }

        sb.appendLine()
        sb.appendLine("====================================================================")
        sb.appendLine("FIN DEL INFORME AUDITABLE - SPAMZERO")
        sb.appendLine("====================================================================")

        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val file = File(exportDir, "Evidencia_SpamZero_$dateFileName.txt")
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Informe Auditado de Evidencia - SpamZero")
            putExtra(Intent.EXTRA_TEXT, "Adjunto informe probatorio de registros de spam y acoso telefónico generado por SpamZero.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Descargar / Compartir Informe Probatorio (.txt)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
