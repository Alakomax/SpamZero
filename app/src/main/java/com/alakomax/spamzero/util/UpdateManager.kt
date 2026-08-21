package com.alakomax.spamzero.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersionName: String,
    val downloadUrl: String,
    val releaseNotes: String
)

object UpdateManager {

    private const val GITHUB_RELEASES_API = "https://api.github.com/repos/Alakomax/SpamZero/releases/latest"

    /**
     * Consulta la API de GitHub para verificar si existe una nueva versión lanzada.
     */
    suspend fun checkForUpdates(currentVersionName: String): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL(GITHUB_RELEASES_API)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == 200) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonString)

                val tagName = json.optString("tag_name", "").trim()
                val body = json.optString("body", "Sin notas de versión.")

                var apkDownloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null && assets.length() > 0) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.optString("browser_download_url", "")
                            break
                        }
                    }
                }

                if (apkDownloadUrl.isBlank() && tagName.isNotBlank()) {
                    apkDownloadUrl = "https://github.com/Alakomax/SpamZero/releases/download/$tagName/app-debug.apk"
                }

                val cleanLatest = tagName.removePrefix("v").trim()
                val cleanCurrent = currentVersionName.removePrefix("v").trim()

                val isNewer = isVersionNewer(cleanLatest, cleanCurrent)

                return@withContext UpdateInfo(
                    hasUpdate = isNewer,
                    latestVersionName = tagName,
                    downloadUrl = apkDownloadUrl,
                    releaseNotes = body
                )
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error al verificar actualizaciones: ${e.message}")
        }
        return@withContext UpdateInfo(false, currentVersionName, "", "")
    }

    /**
     * Descarga el APK e inicia el instalador del sistema de Android.
     */
    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Int) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val destinationFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "SpamZero_Update.apk"
            )

            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.connect()

            val fileLength = connection.contentLength
            val inputStream = connection.inputStream
            val outputStream = FileOutputStream(destinationFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int

            while (inputStream.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (fileLength > 0) {
                    val progress = ((total * 100) / fileLength).toInt()
                    withContext(Dispatchers.Main) {
                        onProgress(progress)
                    }
                }
                outputStream.write(data, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            withContext(Dispatchers.Main) {
                promptInstall(context, destinationFile)
            }
        } catch (e: Exception) {
            Log.e("UpdateManager", "Error descargando APK: ${e.message}")
            withContext(Dispatchers.Main) {
                onError("Error descargando archivo: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Despliega la interfaz de instalación del paquete de Android.
     */
    fun promptInstall(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    private fun isVersionNewer(latest: String, current: String): Boolean {
        if (latest.isBlank()) return false
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until length) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
