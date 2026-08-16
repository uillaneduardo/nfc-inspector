package com.nfcinspector.app.mifare

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
        val result = MifareAccessBitsParser.parse(bytes, 0x69.toByte())

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
        val dataBlock = result.blockPermissions[0]
        assertEquals(0, dataBlock.c1)
        assertEquals(0, dataBlock.c2)
        assertEquals(0, dataBlock.c3)
        assertEquals("Key A|B", dataBlock.readAccess)
        assertEquals("Key A|B", dataBlock.writeAccess)
        assertEquals("Key A|B", dataBlock.incrementAccess)
        assertEquals("Key A|B", dataBlock.decrementTransferRestoreAccess)
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
}
