package com.nfcinspector.app.domain.model

/**
 * Categorization of NFC reader hardware and transport origins.
 *
 * [wireName] is an immutable, standardized identifier used in JSON export,
 * API contracts, and persistent storage.
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
    val readerName: String = sourceType.displayName,
    val readerId: String? = if (sourceType == ReaderSourceType.ANDROID_NFC) "internal_android_adapter" else null,
    val transport: String = sourceType.wireName
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
            readerId = null,
            transport = "imported"
        )

        /**
         * Central helper for reconstructing a [ReaderSource] from persistent storage or external payloads.
         *
         * Ensures that:
         * 1. Transport matches the source type's canonical [ReaderSourceType.wireName] when not explicitly overridden.
         * 2. The default [readerId] "internal_android_adapter" is NEVER erroneously assigned to non-Android readers.
         */
        fun fromPersisted(
            sourceTypeStr: String?,
            readerName: String? = null,
            readerId: String? = null,
            transport: String? = null
        ): ReaderSource {
            val type = ReaderSourceType.fromDbString(sourceTypeStr)
            val name = readerName?.takeIf { it.isNotBlank() } ?: type.displayName

            val cleanedId = when {
                !readerId.isNullOrBlank() && readerId != "internal_android_adapter" -> readerId
                type == ReaderSourceType.ANDROID_NFC -> "internal_android_adapter"
                else -> null
            }

            val resolvedTransport = transport?.takeIf { it.isNotBlank() } ?: type.wireName

            return ReaderSource(
                sourceType = type,
                readerName = name,
                readerId = cleanedId,
                transport = resolvedTransport
            )
        }
    }
}
