package com.nfcinspector.app.domain.model

/**
 * Categorization of NFC reader hardware and transport origins.
 */
enum class ReaderSourceType(val displayName: String, val wireName: String) {
    ANDROID_NFC("NFC Interno Android", "android_nfc"),
    USB("Leitor USB Externo", "usb"),
    BLUETOOTH("Leitor Bluetooth", "bluetooth"),
    REMOTE("Leitor de Rede / Remoto", "remote"),
    IMPORTED("Arquivo / Importação", "imported"),
    UNKNOWN("Desconhecido", "unknown");

    companion object {
        fun fromWireName(wireName: String?): ReaderSourceType {
            if (wireName.isNullOrBlank()) return UNKNOWN
            return entries.firstOrNull { it.wireName.equals(wireName.trim(), ignoreCase = true) } ?: UNKNOWN
        }

        fun fromDbString(name: String?): ReaderSourceType {
            if (name.isNullOrBlank()) return ANDROID_NFC
            return try {
                valueOf(name.trim().uppercase())
            } catch (_: Exception) {
                fromWireName(name)
            }
        }
    }
}

/**
 * Metadata identifying the reader and transport that performed the tag acquisition.
 */
data class ReaderSource(
    val sourceType: ReaderSourceType = ReaderSourceType.ANDROID_NFC,
    val readerName: String = "NFC Interno Android",
    val readerId: String? = "internal_android_adapter",
    val transport: String = "android_nfc"
) {
    val displayName: String
        get() = if (readerName.isNotBlank() && readerName != sourceType.displayName) {
            "${sourceType.displayName} ($readerName)"
        } else {
            sourceType.displayName
        }

    companion object {
        val INTERNAL_NFC = ReaderSource(
            sourceType = ReaderSourceType.ANDROID_NFC,
            readerName = "NFC Interno Android",
            readerId = "internal_android_adapter",
            transport = "android_nfc"
        )

        val IMPORTED_FILE = ReaderSource(
            sourceType = ReaderSourceType.IMPORTED,
            readerName = "Importação de Arquivo",
            readerId = "file_import",
            transport = "file"
        )
    }
}
