package com.nfcinspector.app.report

import com.nfcinspector.app.data.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class ReportFormatterTest {

    private fun createSampleTagRecord(): TagRecord {
        val block0Bytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
        val sector0 = MifareSectorData(
            sectorIndex = 0,
            blockCount = 4,
            firstBlockIndex = 0,
            status = MifareSectorStatus.READ_SUCCESS,
            authKeyType = "Key A",
            authKeyName = "Padrão de Fábrica (NXP)",
            blocks = listOf(
                MifareBlockData(
                    blockIndex = 0,
                    blockIndexInSector = 0,
                    blockType = MifareBlockType.MANUFACTURER,
                    rawBytes = block0Bytes,
                    hexFormatted = "01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10",
                    asciiFormatted = "................",
                    isReadSuccess = true
                ),
                MifareBlockData(
                    blockIndex = 1,
                    blockIndexInSector = 1,
                    blockType = MifareBlockType.DATA,
                    rawBytes = null,
                    hexFormatted = "Não lido / Protegido",
                    asciiFormatted = "—",
                    isReadSuccess = false
                )
            ),
            accessBits = MifareAccessBits(
                rawBytesHex = "FF 07 80",
                gpbHex = "0x69",
                isValid = true,
                trailerPermissions = TrailerAccessPermissions(
                    c1 = 0, c2 = 0, c3 = 1,
                    keyARead = "Nunca", keyAWrite = "Key A",
                    accessBitsRead = "Key A", accessBitsWrite = "Nunca",
                    keyBRead = "Key A", keyBWrite = "Key A"
                ),
                blockPermissions = listOf(
                    BlockAccessPermissions(
                        blockRangeLabel = "Bloco 0", groupIndex = 0,
                        c1 = 0, c2 = 0, c3 = 0,
                        readAccess = "Key A|B", writeAccess = "Key A|B",
                        incrementAccess = "Key A|B", decrementTransferRestoreAccess = "Key A|B"
                    )
                )
            )
        )

        val memoryMap = MifareClassicMemoryMap(
            typeName = "MIFARE Classic 1K",
            sizeBytes = 1024,
            sectorCount = 16,
            blockCount = 64,
            sectors = listOf(sector0),
            isInspected = true,
            authenticatedSectorsCount = 1,
            fullyReadSectorsCount = 0,
            totalBlocksReadCount = 1
        )

        return TagRecord(
            id = 1L,
            timestamp = 1773000000000L,
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologies = listOf("android.nfc.tech.NfcA", "android.nfc.tech.MifareClassic"),
            nfcA = NfcAParams(
                atqaHex = "00 04",
                sakHex = "08",
                timeoutMs = 500,
                maxTransceiveBytes = 253
            ),
            mifareClassic = MifareClassicParams(
                typeName = "MIFARE Classic 1K",
                sizeBytes = 1024,
                sectorCount = 16,
                blockCount = 64,
                memoryMap = memoryMap
            )
        )
    }

    @Test
    fun testGenerateTechnicalReportV2Structure() {
        val tag = createSampleTagRecord()
        val report = ReportFormatter.generateTechnicalReport(tag)

        assertTrue(report.contains("NFC INSPECTOR"))
        assertTrue(report.contains("Relatório Técnico de Inspeção NFC"))
        assertTrue(report.contains("04:5A:B2:1A"))
        assertTrue(report.contains("045AB21A"))
        assertTrue(report.contains("73052698"))
        assertTrue(report.contains("--- NFC-A (ISO 14443-3A) ---"))
        assertTrue(report.contains("--- MIFARE CLASSIC ---"))
        assertTrue(report.contains("--- RESULTADO DA INSPEÇÃO MIFARE CLASSIC ---"))
        assertTrue(report.contains("--- MAPA DE MEMÓRIA DETALHADO ---"))
        assertTrue(report.contains("Setor 00"))
        assertTrue(report.contains("Bloco 00 — Manufacturer Block"))
        assertTrue(report.contains("01 02 03 04 05 06 07 08 09 0A 0B 0C 0D 0E 0F 10"))
        assertTrue(report.contains("Bloco 01 — Data Block"))
        assertTrue(report.contains("[Não lido / Protegido]"))
        assertTrue(report.contains("Bits de Acesso"))
        assertTrue(report.contains("OBSERVAÇÕES E NOTAS DE SEGURANÇA"))
    }

    @Test
    fun testGenerateJsonExportSchemaV1() {
        val tag = createSampleTagRecord()
        val jsonStr = ReportFormatter.generateJsonExport(tag)

        val json = JSONObject(jsonStr)
        assertEquals(1, json.getInt("schemaVersion"))

        val generator = json.getJSONObject("generator")
        assertEquals("NFC Inspector", generator.getString("name"))
        assertEquals("1.0.0", generator.getString("version"))

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
    fun testExportFileNameSanitization() {
        val tag = createSampleTagRecord()
        val fileName = ReportFormatter.getExportFileName(tag, "json")
        assertTrue(fileName.startsWith("nfc-inspector_045AB21A_"))
        assertTrue(fileName.endsWith(".json"))
        assertFalse(fileName.contains(":"))
        assertFalse(fileName.contains(" "))
    }
}
