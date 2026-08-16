package com.nfcinspector.app.data.model

import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.report.ReportFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class TagRecord(
    val id: Long = 0L,
    val scanId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val readerSource: ReaderSource = ReaderSource.INTERNAL_NFC,
    val uidColonHex: String,
    val uidContinuousHex: String,
    val uidDecimal: String,
    val uidLengthBytes: Int,
    val mainTechnology: String,
    val technologies: List<String>,
    val nfcA: NfcAParams? = null,
    val nfcB: NfcBParams? = null,
    val isoDep: IsoDepParams? = null,
    val mifareClassic: MifareClassicParams? = null,
    val mifareUltralight: MifareUltralightParams? = null,
    val nfcF: NfcFParams? = null,
    val nfcV: NfcVParams? = null,
    val ndef: NdefParams? = null,
    val isNdefFormatable: Boolean = false,
    val scanNotes: String = "UID é um identificador de camada física e não deve ser considerado automaticamente uma credencial segura."
) {
    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun generateFullReport(): String {
        return ReportFormatter.generateTechnicalReport(this)
    }

    fun generateJsonExport(): String {
        return ReportFormatter.generateJsonExport(this)
    }

    fun getExportFileName(extension: String = "json"): String {
        return ReportFormatter.getExportFileName(this, extension)
    }
}


