package com.nfcinspector.app.data.model

data class NfcAParams(
    val atqaHex: String,
    val sakHex: String,
    val timeoutMs: Int,
    val maxTransceiveBytes: Int
)

data class NfcBParams(
    val appDataHex: String,
    val protocolInfoHex: String,
    val maxTransceiveBytes: Int
)

data class IsoDepParams(
    val historicalBytesHex: String?,
    val hiLayerResponseHex: String?,
    val isExtendedLengthApduSupported: Boolean,
    val timeoutMs: Int,
    val maxTransceiveBytes: Int
)

data class MifareClassicParams(
    val typeName: String,
    val sizeBytes: Int,
    val sectorCount: Int,
    val blockCount: Int,
    val note: String = "Suporte ao MIFARE Classic depende do chipset NFC do aparelho (NXP). A ausência dessa tecnologia na API não descarta que o cartão seja MIFARE."
)

data class MifareUltralightParams(
    val typeName: String,
    val maxTransceiveBytes: Int,
    val timeoutMs: Int
)

data class NfcFParams(
    val systemCodeHex: String?,
    val manufacturerResponseHex: String?,
    val timeoutMs: Int,
    val maxTransceiveBytes: Int
)

data class NfcVParams(
    val dsfidHex: String?,
    val responseFlagsHex: String?,
    val maxTransceiveBytes: Int
)

data class NdefRecordItem(
    val id: String,
    val tnfName: String,
    val typeString: String,
    val isText: Boolean,
    val isUri: Boolean,
    val isMime: Boolean,
    val isExternal: Boolean,
    val textLanguage: String? = null,
    val textContent: String? = null,
    val uriContent: String? = null,
    val mimeType: String? = null,
    val rawPayloadHex: String
)

data class NdefParams(
    val isWritable: Boolean,
    val canMakeReadOnly: Boolean,
    val typeName: String,
    val currentSizeBytes: Int,
    val maxSizeBytes: Int,
    val recordCount: Int,
    val records: List<NdefRecordItem>
)
