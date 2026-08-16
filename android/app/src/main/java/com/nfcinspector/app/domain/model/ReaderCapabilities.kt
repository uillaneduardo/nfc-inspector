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
         * Standard capabilities provided by Android Internal NFC hardware (Reader Mode).
         */
        val ANDROID_INTERNAL_READER_MODE = ReaderCapabilities(
            capabilities = setOf(
                ReaderCapability.READ,
                ReaderCapability.ISO_DEP,
                ReaderCapability.MIFARE_CLASSIC,
                ReaderCapability.NDEF,
                ReaderCapability.ISO_15693,
                ReaderCapability.FELICA,
                ReaderCapability.RAW_TRANSCEIVE,
                ReaderCapability.APDU
            )
        )
    }
}
