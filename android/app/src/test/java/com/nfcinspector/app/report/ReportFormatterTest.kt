package com.nfcinspector.app.report

import com.nfcinspector.app.data.model.*
import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.domain.model.ReaderSourceType
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReportFormatterTest {

    private fun createSampleTagRecord(): TagRecord {
        return TagRecord(
            id = 1L,
            scanId = "test-uuid-1234-5678",
            timestamp = 1773000000000L,
            readerSource = ReaderSource.INTERNAL_NFC,
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologies = listOf("android.nfc.tech.NfcA", "android.nfc.tech.MifareClassic"),
            nfcA = NfcAParams(
                atqaHex = "00 04",
                sakHex = "08",
                sakDec = 8,
                maxTransceiveBytes = 253,
                timeoutMs = 618
            ),
            mifareClassic = MifareClassicParams(
                sizeBytes = 1024,
                sectorCount = 16,
                blockCount = 64,
                typeString = "MIFARE Classic 1K",
                sectors = listOf(
                    MifareSectorData(
                        sectorIndex = 0,
                        authenticated = true,
                        keyTypeUsed = "Key A",
                        blocks = listOf(
                            MifareBlockData(
                                blockIndex = 0,
                                dataHex = "0102030405060708090A0B0C0D0E0F10",
                                isSectorTrailer = false,
                                isReadable = true
                            ),
                            MifareBlockData(
                                blockIndex = 1,
                                dataHex = null,
                                isSectorTrailer = false,
                                isReadable = false
                            )
                        )
                    )
                )
            ),
            isNdefFormatable = false
        )
    }

    @Test
    fun testGenerateTechnicalReportContainsBasicMetadata() {
        val tag = createSampleTagRecord()
        val report = ReportFormatter.generateTechnicalReport(tag)

        assertTrue(report.contains("NFC INSPECTOR"))
        assertTrue(report.contains("Relatório Técnico de Inspeção NFC"))
        assertTrue(report.contains("ID da Leitura (UUID): test-uuid-1234-5678"))
        assertTrue(report.contains("Origem da Leitura:    NFC Interno Android"))
        assertTrue(report.contains("04:5A:B2:1A"))
        assertTrue(report.contains("045AB21A"))
        assertTrue(report.contains("73052698"))
        assertTrue(report.contains("MIFARE Classic 1K"))
        assertTrue(report.contains("00 04"))
        assertTrue(report.contains("08"))
        assertTrue(report.contains("Gerado por NFC Inspector"))
    }

    @Test
    fun testGenerateTechnicalReportContextualNotes() {
        // Tag with only NFC-B (no NFC-A and no MifareClassic)
        val nfcBTag = TagRecord(
            id = 2L,
            timestamp = 1773000000000L,
            uidColonHex = "08:11:22:33:44:55",
            uidContinuousHex = "081122334455",
            uidDecimal = "123456789",
            uidLengthBytes = 6,
            mainTechnology = "NFC-B (ISO 14443-3B)",
            technologies = listOf("android.nfc.tech.NfcB"),
            nfcB = NfcBParams(
                appDataHex = "01020304",
                protocolInfoHex = "050607",
                maxTransceiveBytes = 253
            )
        )
        val report = ReportFormatter.generateTechnicalReport(nfcBTag)
        assertFalse("Should not mention ISO 14443-3A on an NFC-B only tag", report.contains("ISO 14443-3A"))
        assertFalse("Should not mention Crypto-1 on an NFC-B only tag", report.contains("Crypto-1"))
        assertTrue("Should mention offline privacy on any report", report.contains("Privacidade: Operação offline."))
    }

    @Test
    fun testGenerateJsonExportSchemaV1() {
        val tag = createSampleTagRecord()
        val jsonStr = ReportFormatter.generateJsonExport(tag)

        val json = JSONObject(jsonStr)
        assertEquals(1, json.getInt("schemaVersion"))
        assertEquals("test-uuid-1234-5678", json.getString("scanId"))

        val generator = json.getJSONObject("generator")
        assertEquals("NFC Inspector", generator.getString("name"))
        assertEquals("Android", generator.getString("platform"))

        val reader = json.getJSONObject("reader")
        assertEquals("android_nfc", reader.getString("source"))
        assertEquals("NFC Interno Android", reader.getString("name"))
        assertEquals("android_nfc", reader.getString("transport"))
        assertEquals("internal_android_adapter", reader.getString("id"))

        assertTrue(json.has("capturedAt"))
        assertTrue(json.has("inspectionStatus"))

        val tagObj = json.getJSONObject("tag")
        val uidObj = tagObj.getJSONObject("uid")
        assertEquals("04:5A:B2:1A", uidObj.getString("hexColon"))
        assertEquals("045AB21A", uidObj.getString("hex"))
        assertEquals(4, uidObj.getInt("lengthBytes"))

        val nfcA = json.getJSONObject("nfcA")
        assertEquals("00 04", nfcA.getString("atqa"))
        assertEquals("08", nfcA.getString("sak"))

        val mfc = json.getJSONObject("mifareClassic")
        assertEquals("MIFARE Classic 1K", mfc.getString("variant"))
        assertEquals(1024, mfc.getInt("sizeBytes"))

        val sectors = mfc.getJSONArray("sectors")
        assertEquals(1, sectors.length())

        val sector0 = sectors.getJSONObject(0)
        assertEquals(0, sector0.getInt("sector"))
        val blocks = sector0.getJSONArray("blocks")
        assertEquals(2, blocks.length())

        val block0 = blocks.getJSONObject(0)
        assertTrue(block0.getBoolean("read"))
        assertEquals("0102030405060708090A0B0C0D0E0F10", block0.getString("hex"))

        val block1 = blocks.getJSONObject(1)
        assertFalse(block1.getBoolean("read"))
        assertFalse(block1.has("hex"))

        // Security check: no raw key hex anywhere
        assertFalse(jsonStr.contains("FFFFFFFFFFFF"))
    }

    @Test
    fun testGenerateJsonExportMultipleReaderSources() {
        // USB Reader
        val usbTag = createSampleTagRecord().copy(
            readerSource = ReaderSource(
                sourceType = ReaderSourceType.USB,
                readerName = "ACR122U USB Reader",
                readerId = "usb_072f_2200",
                transport = "usb"
            )
        )
        val usbJson = JSONObject(ReportFormatter.generateJsonExport(usbTag))
        val usbReader = usbJson.getJSONObject("reader")
        assertEquals("usb", usbReader.getString("source"))
        assertEquals("ACR122U USB Reader", usbReader.getString("name"))
        assertEquals("usb", usbReader.getString("transport"))
        assertEquals("usb_072f_2200", usbReader.getString("id"))

        // Bluetooth Reader without ID
        val btTag = createSampleTagRecord().copy(
            readerSource = ReaderSource(
                sourceType = ReaderSourceType.BLUETOOTH,
                readerName = "Leitor Bluetooth",
                readerId = null,
                transport = "bluetooth"
            )
        )
        val btJson = JSONObject(ReportFormatter.generateJsonExport(btTag))
        val btReader = btJson.getJSONObject("reader")
        assertEquals("bluetooth", btReader.getString("source"))
        assertEquals("bluetooth", btReader.getString("transport"))
        assertFalse("When readerId is null, id key should not be present in JSON", btReader.has("id"))

        // Imported File Reader
        val importedTag = createSampleTagRecord().copy(
            readerSource = ReaderSource.IMPORTED_FILE
        )
        val importedJson = JSONObject(ReportFormatter.generateJsonExport(importedTag))
        val importedReader = importedJson.getJSONObject("reader")
        assertEquals("imported", importedReader.getString("source"))
        assertEquals("imported", importedReader.getString("transport"))
    }

    @Test
    fun testExportFileNameSanitization() {
        val tag = createSampleTagRecord()
        val fileName = ReportFormatter.getExportFileName(tag, "json")
        assertTrue(fileName.startsWith("nfc-inspector_045AB21A_"))
        assertTrue(fileName.endsWith(".json"))
        assertFalse(fileName.contains(":"))
        assertFalse(fileName.contains(" "))
    }
}
