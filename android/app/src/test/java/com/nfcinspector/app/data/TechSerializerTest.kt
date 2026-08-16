package com.nfcinspector.app.data

import com.nfcinspector.app.data.local.TechSerializer
import com.nfcinspector.app.data.model.*
import org.junit.Assert.*
import org.junit.Test

class TechSerializerTest {

    @Test
    fun testNfcASerializationRoundTrip() {
        val original = NfcAParams(
            atqaHex = "00 04",
            sakHex = "08",
            timeoutMs = 500,
            maxTransceiveBytes = 253
        )
        val json = TechSerializer.serializeNfcA(original)
        assertNotNull(json)

        val restored = TechSerializer.deserializeNfcA(json)
        assertNotNull(restored)
        assertEquals(original.atqaHex, restored?.atqaHex)
        assertEquals(original.sakHex, restored?.sakHex)
        assertEquals(original.timeoutMs, restored?.timeoutMs)
        assertEquals(original.maxTransceiveBytes, restored?.maxTransceiveBytes)
    }

    @Test
    fun testMifareClassicMemoryMapSerializationWithByteArrayIntegrity() {
        val rawBlock0 = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
        val rawTrailer = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0x07.toByte(), 0x80.toByte(), 0x69.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())

        val blocks = listOf(
            MifareBlockData(
                blockIndex = 0,
                blockIndexInSector = 0,
                blockType = MifareBlockType.MANUFACTURER,
                rawBytes = rawBlock0,
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
                isReadSuccess = false,
                readError = "Authentication failed"
            ),
            MifareBlockData(
                blockIndex = 3,
                blockIndexInSector = 3,
                blockType = MifareBlockType.SECTOR_TRAILER,
                rawBytes = rawTrailer,
                hexFormatted = "FF FF FF FF FF FF FF 07 80 69 FF FF FF FF FF FF",
                asciiFormatted = "................",
                isReadSuccess = true
            )
        )

        val accessBits = MifareAccessBits(
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

        val sector = MifareSectorData(
            sectorIndex = 0,
            blockCount = 4,
            firstBlockIndex = 0,
            status = MifareSectorStatus.READ_SUCCESS,
            authKeyType = "Key A",
            authKeyName = "Padrão de Fábrica (NXP)",
            authKeyUsedHex = "FFFFFFFFFFFF", // Should be sanitized
            blocks = blocks,
            accessBits = accessBits
        )

        val memoryMap = MifareClassicMemoryMap(
            typeName = "MIFARE Classic 1K",
            sizeBytes = 1024,
            sectorCount = 16,
            blockCount = 64,
            sectors = listOf(sector),
            isInspected = true,
            authenticatedSectorsCount = 1,
            fullyReadSectorsCount = 0,
            totalBlocksReadCount = 2
        )

        val originalParams = MifareClassicParams(
            typeName = "MIFARE Classic 1K",
            sizeBytes = 1024,
            sectorCount = 16,
            blockCount = 64,
            memoryMap = memoryMap
        )

        val json = TechSerializer.serializeMifareClassic(originalParams)
        assertNotNull(json)
        // Verify key sanitization: raw hex key FFFFFFFFFFFF must NOT be present in JSON
        assertFalse("Secret hex key should not be serialized", json!!.contains("FFFFFFFFFFFF"))

        val restored = TechSerializer.deserializeMifareClassic(json)
        assertNotNull(restored)
        assertEquals("MIFARE Classic 1K", restored?.typeName)
        assertEquals(1024, restored?.sizeBytes)

        val restoredMap = restored?.memoryMap
        assertNotNull(restoredMap)
        assertEquals(1, restoredMap?.sectors?.size)

        val restoredSec = restoredMap?.sectors?.get(0)
        assertEquals(0, restoredSec?.sectorIndex)
        assertEquals(MifareSectorStatus.READ_SUCCESS, restoredSec?.status)
        assertEquals("Key A", restoredSec?.authKeyType)
        assertEquals("Padrão de Fábrica (NXP)", restoredSec?.authKeyName)
        assertNull("authKeyUsedHex should be null for security", restoredSec?.authKeyUsedHex)

        // Verify ByteArray exact match
        val restoredBlock0 = restoredSec?.blocks?.get(0)
        assertTrue(restoredBlock0?.isReadSuccess == true)
        assertArrayEquals(rawBlock0, restoredBlock0?.rawBytes)

        // Verify unread block
        val restoredBlock1 = restoredSec?.blocks?.get(1)
        assertFalse(restoredBlock1?.isReadSuccess == true)
        assertNull(restoredBlock1?.rawBytes)
        assertEquals("Authentication failed", restoredBlock1?.readError)

        // Verify AccessBits
        val restoredAb = restoredSec?.accessBits
        assertNotNull(restoredAb)
        assertTrue(restoredAb?.isValid == true)
        assertEquals("FF 07 80", restoredAb?.rawBytesHex)
        assertEquals("0x69", restoredAb?.gpbHex)
        assertEquals(1, restoredAb?.blockPermissions?.size)
        assertEquals("Key A|B", restoredAb?.blockPermissions?.get(0)?.readAccess)
    }

    @Test
    fun testNullOrCorruptJsonHandling() {
        assertNull(TechSerializer.deserializeNfcA(null))
        assertNull(TechSerializer.deserializeNfcA(""))
        assertNull(TechSerializer.deserializeNfcA("{ invalid json ..."))

        assertNull(TechSerializer.deserializeMifareClassic(null))
        assertNull(TechSerializer.deserializeMifareClassic("{ corrupted: [1,2,3"))
    }

    @Test
    fun testNdefSerializationRoundTrip() {
        val records = listOf(
            NdefRecordItem(
                id = "1",
                tnfName = "Well Known (0x01)",
                typeString = "URI (0x55)",
                isText = false,
                isUri = true,
                isMime = false,
                isExternal = false,
                uriContent = "https://github.com/nfcinspector",
                rawPayloadHex = "046769746875622E636F6D"
            )
        )
        val original = NdefParams(
            isWritable = true,
            canMakeReadOnly = false,
            typeName = "NFC Forum Type 2",
            currentSizeBytes = 32,
            maxSizeBytes = 137,
            recordCount = 1,
            records = records
        )

        val json = TechSerializer.serializeNdef(original)
        assertNotNull(json)

        val restored = TechSerializer.deserializeNdef(json)
        assertNotNull(restored)
        assertEquals("NFC Forum Type 2", restored?.typeName)
        assertEquals(true, restored?.isWritable)
        assertEquals(1, restored?.recordCount)
        assertEquals("https://github.com/nfcinspector", restored?.records?.get(0)?.uriContent)
    }
}
