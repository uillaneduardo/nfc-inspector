package com.nfcinspector.app.report

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.nfcinspector.app.data.model.TagRecord
import java.io.File
import java.io.FileOutputStream

/**
 * Helper utility for saving and sharing report files (JSON and TXT)
 * using the Storage Access Framework (SAF) and secure content URIs via FileProvider.
 */
object ReportExportHelper {

    /**
     * Writes textual content to a user-selected URI obtained from ACTION_CREATE_DOCUMENT.
     */
    fun saveContentToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                Toast.makeText(context, "Erro ao abrir destino para gravação.", Toast.LENGTH_LONG).show()
                return false
            }
            outputStream.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
                stream.flush()
            }
            Toast.makeText(context, "Arquivo salvo com sucesso!", Toast.LENGTH_LONG).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao salvar arquivo: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * Shares report content as a file via FileProvider, falling back to EXTRA_TEXT if needed.
     */
    fun shareReportFile(
        context: Context,
        tag: TagRecord,
        content: String,
        extension: String,
        mimeType: String,
        chooserTitle: String
    ) {
        val fileName = ReportFormatter.getExportFileName(tag, extension)
        try {
            val cacheFolder = File(context.cacheDir, "reports")
            if (!cacheFolder.exists()) {
                cacheFolder.mkdirs()
            }
            val tempFile = File(cacheFolder, fileName)
            FileOutputStream(tempFile).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, tempFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, content) // Fallback for receivers that prefer inline text
                putExtra(Intent.EXTRA_TITLE, fileName)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Safe fallback without FileProvider
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, chooserTitle))
        }
    }
}
