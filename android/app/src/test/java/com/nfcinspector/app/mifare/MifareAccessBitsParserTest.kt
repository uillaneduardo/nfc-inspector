package com.nfcinspector.app.mifare

import com.nfcinspector.app.data.model.MifareClassicMemoryMap
import com.nfcinspector.app.nfc.mifare.MifareAccessBitsParser
import com.nfcinspector.app.nfc.mifare.MifareClassicInspector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MifareAccessBitsParserTest {

    @Test
    fun testFactoryDefaultTransportConfiguration() {
        // Factory default transport configuration: FF 07 80 (GPB: 69)
        // Byte 6: 0xFF, Byte 7: 0x07, Byte 8: 0x80
        val bytes = byteArrayOf(0xFF.toByte(), 0x07.toByte(), 0x80.toByte())
        val result = MifareAccessBitsParser.parse(bytes, 0x69.toByte(), blockCountInSector = 4)

        assertTrue("Access bits should be valid", result.isValid)
        assertEquals("FF 07 80", result.rawBytesHex)
        assertEquals("0x69", result.gpbHex)

        // Sector trailer permissions
        val trailer = result.trailerPermissions
        assertNotNull(trailer)
        assertEquals(0, trailer?.c1)
        assertEquals(0, trailer?.c2)
        assertEquals(1, trailer?.c3)
        assertEquals("Key A", trailer?.keyAWrite)
        assertEquals("Key A", trailer?.accessBitsRead)
        assertEquals("Nunca", trailer?.accessBitsWrite)
        assertEquals("Key A", trailer?.keyBRead)
        assertEquals("Key A", trailer?.keyBWrite)

        // Data blocks permissions (Block 0, 1, 2)
        assertEquals(3, result.blockPermissions.size)
        val dataBlock0 = result.blockPermissions[0]
        assertEquals("Bloco 0", dataBlock0.blockRangeLabel)
        assertEquals(0, dataBlock0.c1)
        assertEquals(0, dataBlock0.c2)
        assertEquals(0, dataBlock0.c3)
        assertEquals("Key A|B", dataBlock0.readAccess)
        assertEquals("Key A|B", dataBlock0.writeAccess)
        assertEquals("Key A|B", dataBlock0.incrementAccess)
        assertEquals("Key A|B", dataBlock0.decrementTransferRestoreAccess)
    }

    @Test
    fun testLargeSector4KAccessBits() {
        // MIFARE Classic 4K large sector with 16 blocks (sectors 32-39)
        val bytes = byteArrayOf(0xFF.toByte(), 0x07.toByte(), 0x80.toByte())
        val result = MifareAccessBitsParser.parse(bytes, 0x69.toByte(), blockCountInSector = 16)

        assertTrue("Access bits should be valid", result.isValid)
        assertEquals(3, result.blockPermissions.size)

        assertEquals("Blocos 0..4 (Grupo 0)", result.blockPermissions[0].blockRangeLabel)
        assertEquals("Blocos 5..9 (Grupo 1)", result.blockPermissions[1].blockRangeLabel)
        assertEquals("Blocos 10..14 (Grupo 2)", result.blockPermissions[2].blockRangeLabel)
    }

    @Test
    fun testReadOnlyDataBlockPermissions() {
        // C1=0, C2=1, C3=0 -> Read Key A|B, Write Nunca, Inc Nunca, Dec Nunca
        // Let's compute Access bits for Group 0: C1_0=0, C2_0=1, C3_0=0
        // and default for others
        // Invert Byte 6: ~C2_3..~C2_0, ~C1_3..~C1_0
        // We can verify evaluateDataBlockPermissions directly
        val perms = MifareAccessBitsParser.evaluateDataBlockPermissions(0, 0, 1, 0, 4)
        assertEquals("Bloco 0", perms.blockRangeLabel)
        assertEquals("Key A|B", perms.readAccess)
        assertEquals("Nunca", perms.writeAccess)
        assertEquals("Nunca", perms.incrementAccess)
        assertEquals("Nunca", perms.decrementTransferRestoreAccess)
    }

    @Test
    fun testValueBlockPermissions() {
        // C1=1, C2=1, C3=0 -> Read Key A|B, Write Key B, Inc Key B, Dec Key A|B
        val perms = MifareAccessBitsParser.evaluateDataBlockPermissions(1, 1, 1, 0, 4)
        assertEquals("Bloco 1", perms.blockRangeLabel)
        assertEquals("Key A|B", perms.readAccess)
        assertEquals("Key B", perms.writeAccess)
        assertEquals("Key B", perms.incrementAccess)
        assertEquals("Key A|B", perms.decrementTransferRestoreAccess)
    }

    @Test
    fun testInconsistentAccessBitsDetected() {
        // Corrupted bits where complement does not match
        val bytes = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte())
        val result = MifareAccessBitsParser.parse(bytes, 0x00.toByte())

        assertFalse("Access bits with invalid complements must be marked invalid", result.isValid)
        assertNotNull(result.inconsistencyError)
    }

    @Test
    fun testCapacityFormatting() {
        assertEquals("320 bytes (Mini)", MifareClassicMemoryMap.formatMifareCapacity(320))
        assertEquals("1024 bytes (1 KB)", MifareClassicMemoryMap.formatMifareCapacity(1024))
        assertEquals("2048 bytes (2 KB)", MifareClassicMemoryMap.formatMifareCapacity(2048))
        assertEquals("4096 bytes (4 KB)", MifareClassicMemoryMap.formatMifareCapacity(4096))
        assertEquals("512 bytes", MifareClassicMemoryMap.formatMifareCapacity(512))

        assertEquals("320B", MifareClassicMemoryMap.formatMifareCapacityShort(320))
        assertEquals("1 KB", MifareClassicMemoryMap.formatMifareCapacityShort(1024))
        assertEquals("4 KB", MifareClassicMemoryMap.formatMifareCapacityShort(4096))
    }

    @Test
    fun testAsciiSafeSanitization() {
        val nonPrintable = byteArrayOf(0x00, 0x01, 0x02, 0x1F, 0x7F, 0x48, 0x65, 0x6C, 0x6C, 0x6F)
        val ascii = MifareClassicInspector.formatAsciiSafe(nonPrintable)
        assertEquals(".....Hello", ascii)
    }

    @Test
    fun testParseHexKey() {
        val validKey = MifareClassicInspector.parseHexKey("FF FF FF FF FF FF")
        assertNotNull(validKey)
        assertEquals(6, validKey?.size)

        val invalidKey = MifareClassicInspector.parseHexKey("FFAABB")
        assertEquals(null, invalidKey)
    }

    @Test
    fun testResolveVariantTypeName() {
        assertEquals("MIFARE Classic Mini", MifareClassicInspector.resolveVariantTypeName(0, 320, 5))
        assertEquals("MIFARE Classic 1K", MifareClassicInspector.resolveVariantTypeName(0, 1024, 16))
        assertEquals("MIFARE Classic 2K", MifareClassicInspector.resolveVariantTypeName(0, 2048, 32))
        assertEquals("MIFARE Classic 4K", MifareClassicInspector.resolveVariantTypeName(0, 4096, 40))
        assertEquals("MIFARE Classic", MifareClassicInspector.resolveVariantTypeName(0, 512, 8))
        assertEquals("MIFARE Plus (SL1 emulado)", MifareClassicInspector.resolveVariantTypeName(1, 2048, 32))
        assertEquals("MIFARE Pro", MifareClassicInspector.resolveVariantTypeName(2, 4096, 40))
    }
}

