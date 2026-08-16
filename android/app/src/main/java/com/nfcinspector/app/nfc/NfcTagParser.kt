package com.nfcinspector.app.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import com.nfcinspector.app.data.model.IsoDepParams
import com.nfcinspector.app.data.model.MifareClassicParams
import com.nfcinspector.app.data.model.MifareUltralightParams
import com.nfcinspector.app.data.model.NdefParams
import com.nfcinspector.app.data.model.NdefRecordItem
import com.nfcinspector.app.data.model.NfcAParams
import com.nfcinspector.app.data.model.NfcBParams
import com.nfcinspector.app.data.model.NfcFParams
import com.nfcinspector.app.data.model.NfcVParams
import com.nfcinspector.app.data.model.TagRecord
import java.math.BigInteger
import java.nio.charset.Charset
import java.util.Locale

object NfcTagParser {

    private val URI_PREFIX_MAP = mapOf(
        0x00.toByte() to "",
        0x01.toByte() to "http://www.",
        0x02.toByte() to "https://www.",
        0x03.toByte() to "http://",
        0x04.toByte() to "https://",
        0x05.toByte() to "tel:",
        0x06.toByte() to "mailto:",
        0x07.toByte() to "ftp://anonymous:anonymous@",
        0x08.toByte() to "ftp://ftp.",
        0x09.toByte() to "ftps://",
        0x0A.toByte() to "sftp://",
        0x0B.toByte() to "smb://",
        0x0C.toByte() to "nfs://",
        0x0D.toByte() to "ftp://",
        0x0E.toByte() to "dav://",
        0x0F.toByte() to "news:",
        0x10.toByte() to "telnet://",
        0x11.toByte() to "imap:",
        0x12.toByte() to "rtsp://",
        0x13.toByte() to "urn:",
        0x14.toByte() to "pop:",
        0x15.toByte() to "sip:",
        0x16.toByte() to "sips:",
        0x17.toByte() to "tftp:",
        0x18.toByte() to "btspp://",
        0x19.toByte() to "btl2cap://",
        0x1A.toByte() to "btgoep://",
        0x1B.toByte() to "tcpobex://",
        0x1C.toByte() to "irdaobex://",
        0x1D.toByte() to "file://",
        0x1E.toByte() to "urn:epc:id:",
        0x1F.toByte() to "urn:epc:tag:",
        0x20.toByte() to "urn:epc:pat:",
        0x21.toByte() to "urn:epc:raw:",
        0x22.toByte() to "urn:epc:",
        0x23.toByte() to "urn:nfc:"
    )

    fun parseTag(tag: Tag): TagRecord {
        val rawId = tag.id ?: byteArrayOf()
        val uidColonHex = toColonHex(rawId)
        val uidContinuousHex = toContinuousHex(rawId)
        val uidDecimal = toDecimalString(rawId)
        val uidLengthBytes = rawId.size

        val techList = tag.techList.map { cleanTechName(it) }

        // Determine main classification
        val mainTech = determineMainTech(techList)

        // Parse individual technologies safely
        val nfcAParams = parseNfcA(tag)
        val nfcBParams = parseNfcB(tag)
        val isoDepParams = parseIsoDep(tag)
        val mifareClassicParams = parseMifareClassic(tag)
        val mifareUltralightParams = parseMifareUltralight(tag)
        val nfcFParams = parseNfcF(tag)
        val nfcVParams = parseNfcV(tag)
        val ndefParams = parseNdef(tag)
        val isNdefFormatable = NdefFormatable.get(tag) != null

        return TagRecord(
            uidColonHex = uidColonHex,
            uidContinuousHex = uidContinuousHex,
            uidDecimal = uidDecimal,
            uidLengthBytes = uidLengthBytes,
            mainTechnology = mainTech,
            technologies = techList,
            nfcA = nfcAParams,
            nfcB = nfcBParams,
            isoDep = isoDepParams,
            mifareClassic = mifareClassicParams,
            mifareUltralight = mifareUltralightParams,
            nfcF = nfcFParams,
            nfcV = nfcVParams,
            ndef = ndefParams,
            isNdefFormatable = isNdefFormatable
        )
    }

    private fun cleanTechName(raw: String): String {
        return raw.substringAfterLast(".")
    }

    private fun determineMainTech(techList: List<String>): String {
        return when {
            techList.contains("IsoDep") && techList.contains("NfcA") -> "ISO 14443-4A (ISO-DEP / Smart Card)"
            techList.contains("IsoDep") && techList.contains("NfcB") -> "ISO 14443-4B (ISO-DEP Type B)"
            techList.contains("MifareClassic") -> "NXP MIFARE Classic"
            techList.contains("MifareUltralight") -> "NXP MIFARE Ultralight / NTAG"
            techList.contains("NfcA") && techList.contains("Ndef") -> "NFC Forum Type 2 / Type 4 (NfcA + NDEF)"
            techList.contains("NfcA") -> "ISO 14443-3A (NFC-A)"
            techList.contains("NfcB") -> "ISO 14443-3B (NFC-B)"
            techList.contains("NfcF") -> "JIS 6319-4 (Sony FeliCa / NFC-F)"
            techList.contains("NfcV") -> "ISO 15693 (Vicinity Card / NFC-V)"
            else -> techList.firstOrNull() ?: "Tag NFC Genérica"
        }
    }

    private fun parseNfcA(tag: Tag): NfcAParams? {
        val nfcA = NfcA.get(tag) ?: return null
        return try {
            val atqa = nfcA.atqa
            val sak = nfcA.sak
            val timeout = nfcA.timeout
            val maxTransceive = nfcA.maxTransceiveLength
            NfcAParams(
                atqaHex = "0x" + toContinuousHex(atqa),
                sakHex = String.format(Locale.US, "0x%02X", sak),
                timeoutMs = timeout,
                maxTransceiveBytes = maxTransceive
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNfcB(tag: Tag): NfcBParams? {
        val nfcB = NfcB.get(tag) ?: return null
        return try {
            val appData = nfcB.applicationData ?: byteArrayOf()
            val protoInfo = nfcB.protocolInfo ?: byteArrayOf()
            val maxTransceive = nfcB.maxTransceiveLength
            NfcBParams(
                appDataHex = if (appData.isNotEmpty()) "0x" + toContinuousHex(appData) else "Não especificado",
                protocolInfoHex = if (protoInfo.isNotEmpty()) "0x" + toContinuousHex(protoInfo) else "Não especificado",
                maxTransceiveBytes = maxTransceive
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseIsoDep(tag: Tag): IsoDepParams? {
        val isoDep = IsoDep.get(tag) ?: return null
        return try {
            val histBytes = isoDep.historicalBytes?.let { if (it.isNotEmpty()) "0x" + toContinuousHex(it) else null }
            val hiLayerResp = isoDep.hiLayerResponse?.let { if (it.isNotEmpty()) "0x" + toContinuousHex(it) else null }
            val isExtended = isoDep.isExtendedLengthApduSupported
            val timeout = isoDep.timeout
            val maxTransceive = isoDep.maxTransceiveLength
            IsoDepParams(
                historicalBytesHex = histBytes,
                hiLayerResponseHex = hiLayerResp,
                isExtendedLengthApduSupported = isExtended,
                timeoutMs = timeout,
                maxTransceiveBytes = maxTransceive
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMifareClassic(tag: Tag): MifareClassicParams? {
        val mfc = MifareClassic.get(tag) ?: return null
        return try {
            val typeStr = when (mfc.type) {
                MifareClassic.TYPE_CLASSIC -> "MIFARE Classic Standard"
                MifareClassic.TYPE_PLUS -> "MIFARE Plus"
                MifareClassic.TYPE_PRO -> "MIFARE Pro"
                else -> "MIFARE Desconhecido"
            }
            MifareClassicParams(
                typeName = typeStr,
                sizeBytes = mfc.size,
                sectorCount = mfc.sectorCount,
                blockCount = mfc.blockCount
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseMifareUltralight(tag: Tag): MifareUltralightParams? {
        val mfu = MifareUltralight.get(tag) ?: return null
        return try {
            val typeStr = when (mfu.type) {
                MifareUltralight.TYPE_ULTRALIGHT -> "MIFARE Ultralight"
                MifareUltralight.TYPE_ULTRALIGHT_C -> "MIFARE Ultralight C"
                else -> "MIFARE Ultralight / NTAG compatível"
            }
            MifareUltralightParams(
                typeName = typeStr,
                maxTransceiveBytes = mfu.maxTransceiveLength,
                timeoutMs = mfu.timeout
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNfcF(tag: Tag): NfcFParams? {
        val nfcF = NfcF.get(tag) ?: return null
        return try {
            val systemCode = nfcF.systemCode?.let { if (it.isNotEmpty()) "0x" + toContinuousHex(it) else null }
            val mfr = nfcF.manufacturer?.let { if (it.isNotEmpty()) "0x" + toContinuousHex(it) else null }
            NfcFParams(
                systemCodeHex = systemCode,
                manufacturerResponseHex = mfr,
                timeoutMs = nfcF.timeout,
                maxTransceiveBytes = nfcF.maxTransceiveLength
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNfcV(tag: Tag): NfcVParams? {
        val nfcV = NfcV.get(tag) ?: return null
        return try {
            val dsfid = String.format(Locale.US, "0x%02X", nfcV.dsfId)
            val flags = String.format(Locale.US, "0x%02X", nfcV.responseFlags)
            NfcVParams(
                dsfidHex = dsfid,
                responseFlagsHex = flags,
                maxTransceiveBytes = nfcV.maxTransceiveLength
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseNdef(tag: Tag): NdefParams? {
        val ndef = Ndef.get(tag) ?: return null
        return try {
            val isWritable = ndef.isWritable
            val canMakeReadOnly = ndef.canMakeReadOnly()
            val maxSize = ndef.maxSize
            val typeName = ndef.type ?: "NDEF Forum"

            var currentSize = 0
            var recordsList = emptyList<NdefRecordItem>()

            // Safely read cached message or read directly
            val cachedMsg = ndef.cachedNdefMessage
            val msg: NdefMessage? = cachedMsg ?: try {
                if (!ndef.isConnected) ndef.connect()
                val readMsg = ndef.ndefMessage
                ndef.close()
                readMsg
            } catch (e: Exception) {
                null
            }

            if (msg != null) {
                currentSize = msg.byteArrayLength
                recordsList = msg.records.mapIndexed { idx, record ->
                    parseNdefRecord(record, idx)
                }
            }

            NdefParams(
                isWritable = isWritable,
                canMakeReadOnly = canMakeReadOnly,
                typeName = typeName,
                currentSizeBytes = currentSize,
                maxSizeBytes = maxSize,
                recordCount = recordsList.size,
                records = recordsList
            )
        } catch (e: Exception) {
            null
        } finally {
            try {
                if (ndef.isConnected) ndef.close()
            } catch (_: Exception) {}
        }
    }

    private fun parseNdefRecord(record: NdefRecord, index: Int): NdefRecordItem {
        val tnf = record.tnf
        val tnfName = when (tnf) {
            NdefRecord.TNF_EMPTY -> "TNF_EMPTY (0x00)"
            NdefRecord.TNF_WELL_KNOWN -> "TNF_WELL_KNOWN (0x01)"
            NdefRecord.TNF_MIME_MEDIA -> "TNF_MIME_MEDIA (0x02)"
            NdefRecord.TNF_ABSOLUTE_URI -> "TNF_ABSOLUTE_URI (0x03)"
            NdefRecord.TNF_EXTERNAL_TYPE -> "TNF_EXTERNAL_TYPE (0x04)"
            NdefRecord.TNF_UNKNOWN -> "TNF_UNKNOWN (0x05)"
            NdefRecord.TNF_UNCHANGED -> "TNF_UNCHANGED (0x06)"
            else -> "TNF_RESERVED ($tnf)"
        }

        val typeBytes = record.type ?: byteArrayOf()
        val typeString = String(typeBytes, Charsets.US_ASCII)
        val payload = record.payload ?: byteArrayOf()
        val rawPayloadHex = toContinuousHex(payload)

        var isText = false
        var isUri = false
        var isMime = false
        var isExternal = false

        var textLang: String? = null
        var textBody: String? = null
        var uriBody: String? = null
        var mimeStr: String? = null

        if (tnf == NdefRecord.TNF_WELL_KNOWN) {
            if (typeBytes.contentEquals(NdefRecord.RTD_TEXT)) {
                isText = true
                if (payload.isNotEmpty()) {
                    val statusByte = payload[0].toInt()
                    val isUtf16 = (statusByte and 0x80) != 0
                    val langLength = statusByte and 0x3F
                    val charset = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8
                    if (payload.size >= 1 + langLength) {
                        textLang = String(payload, 1, langLength, Charsets.US_ASCII)
                        textBody = String(payload, 1 + langLength, payload.size - 1 - langLength, charset)
                    }
                }
            } else if (typeBytes.contentEquals(NdefRecord.RTD_URI)) {
                isUri = true
                if (payload.isNotEmpty()) {
                    val prefixByte = payload[0]
                    val prefix = URI_PREFIX_MAP[prefixByte] ?: ""
                    val uriSuffix = String(payload, 1, payload.size - 1, Charsets.UTF_8)
                    uriBody = prefix + uriSuffix
                }
            }
        } else if (tnf == NdefRecord.TNF_MIME_MEDIA) {
            isMime = true
            mimeStr = typeString
        } else if (tnf == NdefRecord.TNF_EXTERNAL_TYPE) {
            isExternal = true
        }

        return NdefRecordItem(
            id = "rec_$index",
            tnfName = tnfName,
            typeString = typeString,
            isText = isText,
            isUri = isUri,
            isMime = isMime,
            isExternal = isExternal,
            textLanguage = textLang,
            textContent = textBody,
            uriContent = uriBody,
            mimeType = mimeStr,
            rawPayloadHex = rawPayloadHex
        )
    }

    fun toColonHex(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "N/A"
        return bytes.joinToString(":") { String.format(Locale.US, "%02X", it) }
    }

    fun toContinuousHex(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "N/A"
        return bytes.joinToString("") { String.format(Locale.US, "%02X", it) }
    }

    fun toDecimalString(bytes: ByteArray): String {
        if (bytes.isEmpty()) return "N/A"
        return try {
            BigInteger(1, bytes).toString(10)
        } catch (e: Exception) {
            "N/A"
        }
    }
}
