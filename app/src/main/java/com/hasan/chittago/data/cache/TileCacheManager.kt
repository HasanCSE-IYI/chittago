package com.hasan.chittago.data.cache

import android.content.Context
import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TileCacheManager(context: Context) {

    private val cacheDir = File(context.cacheDir, "maptiles").apply {
        if (!exists()) mkdirs()
    }

    init {
        // Purge any stale CartoDB tiles that contained "API KEY REQUIRED" watermark
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.name.contains("cartocdn")) {
                    file.delete()
                }
            }
        } catch (_: Exception) {}
    }

    fun isTileRequest(uri: Uri): Boolean {
        val host = uri.host ?: return false
        val path = uri.path ?: return false
        val isAllowedHost = host.contains("openstreetmap.org") ||
                host.contains("openstreetmap.fr") ||
                host.contains("opentopomap.org") ||
                host.contains("arcgisonline.com") ||
                host.contains("rainviewer.com")
        if (!isAllowedHost) return false

        return path.contains("/tile/") ||
                path.contains("/radar/") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".jpeg") ||
                path.endsWith(".webp")
    }

    fun getCachedResponse(uri: Uri): WebResourceResponse? {
        val tileFile = getCacheFileForUri(uri)
        val mimeType = if (uri.toString().contains("arcgisonline") ||
            uri.path?.endsWith(".jpg") == true ||
            uri.path?.endsWith(".jpeg") == true) {
            "image/jpeg"
        } else {
            "image/png"
        }

        if (tileFile.exists() && tileFile.length() > 0) {
            return try {
                WebResourceResponse(mimeType, null, FileInputStream(tileFile))
            } catch (e: Exception) {
                null
            }
        }

        // Fetch synchronously on WebView background resource thread and cache
        return try {
            val url = URL(uri.toString())
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 6000
                readTimeout = 10000
                setRequestProperty("User-Agent", "Chittago-App/1.0 (Mobile; Android; Contact: HasanCSE-IYI)")
            }
            conn.connect()
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val bytes = conn.inputStream.readBytes()
                try {
                    FileOutputStream(tileFile).use { it.write(bytes) }
                } catch (_: Exception) {}
                WebResourceResponse(mimeType, null, bytes.inputStream())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCacheFileForUri(uri: Uri): File {
        val safeKey = uri.toString().replace("[^a-zA-Z0-9_.-]".toRegex(), "_")
        return File(cacheDir, "$safeKey.png")
    }

    suspend fun clearCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            cacheDir.deleteRecursively()
            cacheDir.mkdirs()
            true
        } catch (e: Exception) {
            false
        }
    }
}
