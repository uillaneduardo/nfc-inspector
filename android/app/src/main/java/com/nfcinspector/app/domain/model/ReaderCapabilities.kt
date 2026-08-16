package com.nfcinspector.app.domain.model

/**
 * Functional capabilities supported by an NFC reader / transport adapter.
 */
enum class ReaderCapability {
    READ,
    WRITE,
    ISO_DEP,
    MIFARE_CLASSIC,
    NDEF,
    ISO_15693,
    FELICA,
    RAW_TRANSCEIVE,
    APDU,
    HCE
}

/**
 * Declared capabilities for an active or queried NFC reader interface.
 */
data class ReaderCapabilities(
    val capabilities: Set<ReaderCapability>
) {
    val canRead: Boolean get() = capabilities.contains(ReaderCapability.READ)
    val canWrite: Boolean get() = capabilities.contains(ReaderCapability.WRITE)
    val supportsIsoDep: Boolean get() = capabilities.contains(ReaderCapability.ISO_DEP)
    val supportsMifareClassic: Boolean get() = capabilities.contains(ReaderCapability.MIFARE_CLASSIC)
    val supportsNdef: Boolean get() = capabilities.contains(ReaderCapability.NDEF)
    val supportsIso15693: Boolean get() = capabilities.contains(ReaderCapability.ISO_15693)
    val supportsFelica: Boolean get() = capabilities.contains(ReaderCapability.FELICA)
    val supportsRawTransceive: Boolean get() = capabilities.contains(ReaderCapability.RAW_TRANSCEIVE)
    val supportsApdu: Boolean get() = capabilities.contains(ReaderCapability.APDU)
    val supportsHce: Boolean get() = capabilities.contains(ReaderCapability.HCE)

    fun hasCapability(capability: ReaderCapability): Boolean = capabilities.contains(capability)

    companion object {
        /**
         * Baseline structural capabilities guaranteed by Android Internal Reader Mode.
         * Only universal operations (such as [ReaderCapability.READ]) are declared statically.
         *
         * Hardware/chipset-dependent capabilities (such as MIFARE Classic, FeliCa, ISO-15693,
         * or ISO-DEP APDU exchange) must be confirmed dynamically via [fromDetectedTechnologies].
         */
        val ANDROID_INTERNAL_READER_MODE = ReaderCapabilities(
            capabilities = setOf(ReaderCapability.READ)
        )

        /**
         * Infers confirmed capabilities based on observed technologies in a scan.
         */
        fun fromDetectedTechnologies(techNames: Collection<String>): ReaderCapabilities {
            val caps = mutableSetOf(ReaderCapability.READ)
            val normalized = techNames.map { it.trim().lowercase() }

            if (normalized.any { it.contains("isodep") || it.contains("iso_dep") }) {
                caps.add(ReaderCapability.ISO_DEP)
                caps.add(ReaderCapability.APDU)
            }
            if (normalized.any { it.contains("mifareclassic") || it.contains("mifare_classic") }) {
                caps.add(ReaderCapability.MIFARE_CLASSIC)
            }
            if (normalized.any { it.contains("ndef") }) {
                caps.add(ReaderCapability.NDEF)
            }
            if (normalized.any { it.contains("nfcv") || it.contains("iso15693") || it.contains("15693") }) {
                caps.add(ReaderCapability.ISO_15693)
            }
            if (normalized.any { it.contains("nfcf") || it.contains("felica") }) {
                caps.add(ReaderCapability.FELICA)
            }
            if (normalized.any { it.contains("nfca") || it.contains("nfcb") || it.contains("nfcf") || it.contains("nfcv") }) {
                caps.add(ReaderCapability.RAW_TRANSCEIVE)
            }

            return ReaderCapabilities(caps)
        }
    }
}
