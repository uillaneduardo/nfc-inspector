package com.nfcinspector.app.domain

import com.nfcinspector.app.domain.model.ReaderCapabilities
import com.nfcinspector.app.domain.model.ReaderCapability
import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.domain.model.ReaderSourceType
import org.junit.Assert.*
import org.junit.Test

class ReaderSourceAndCapabilitiesTest {

    @Test
    fun testReaderSourceTypeFromWireName() {
        assertEquals(ReaderSourceType.ANDROID_NFC, ReaderSourceType.fromWireName("android_nfc"))
        assertEquals(ReaderSourceType.USB, ReaderSourceType.fromWireName("usb"))
        assertEquals(ReaderSourceType.BLUETOOTH, ReaderSourceType.fromWireName("bluetooth"))
        assertEquals(ReaderSourceType.REMOTE, ReaderSourceType.fromWireName("remote"))
        assertEquals(ReaderSourceType.IMPORTED, ReaderSourceType.fromWireName("imported"))
        assertEquals(ReaderSourceType.UNKNOWN, ReaderSourceType.fromWireName("unknown_custom"))
        assertEquals(ReaderSourceType.UNKNOWN, ReaderSourceType.fromWireName(null))
        assertEquals(ReaderSourceType.UNKNOWN, ReaderSourceType.fromWireName(""))
    }

    @Test
    fun testReaderSourceTypeFromDbString() {
        assertEquals(ReaderSourceType.ANDROID_NFC, ReaderSourceType.fromDbString("ANDROID_NFC"))
        assertEquals(ReaderSourceType.USB, ReaderSourceType.fromDbString("USB"))
        assertEquals(ReaderSourceType.BLUETOOTH, ReaderSourceType.fromDbString("BLUETOOTH"))
        assertEquals(ReaderSourceType.REMOTE, ReaderSourceType.fromDbString("REMOTE"))
        assertEquals(ReaderSourceType.IMPORTED, ReaderSourceType.fromDbString("IMPORTED"))
        assertEquals(ReaderSourceType.ANDROID_NFC, ReaderSourceType.fromDbString(null))
        assertEquals(ReaderSourceType.ANDROID_NFC, ReaderSourceType.fromDbString(""))
    }

    @Test
    fun testReaderSourceDisplayName() {
        val internal = ReaderSource.INTERNAL_NFC
        assertEquals("NFC Interno Android", internal.displayName)

        val customUsb = ReaderSource(
            sourceType = ReaderSourceType.USB,
            readerName = "ACR122U USB Reader",
            readerId = "usb_072f_2200",
            transport = "usb"
        )
        assertEquals("Leitor USB Externo (ACR122U USB Reader)", customUsb.displayName)
    }

    @Test
    fun testReaderCapabilities() {
        val internalCapabilities = ReaderCapabilities.ANDROID_INTERNAL_READER_MODE
        assertTrue(internalCapabilities.canRead)
        assertTrue(internalCapabilities.supportsIsoDep)
        assertTrue(internalCapabilities.supportsMifareClassic)
        assertTrue(internalCapabilities.supportsNdef)
        assertTrue(internalCapabilities.supportsIso15693)
        assertTrue(internalCapabilities.supportsFelica)
        assertTrue(internalCapabilities.supportsRawTransceive)
        assertTrue(internalCapabilities.supportsApdu)
        assertFalse(internalCapabilities.supportsHce) // Reader mode is not HCE emulation

        val customCaps = ReaderCapabilities(setOf(ReaderCapability.READ, ReaderCapability.HCE))
        assertTrue(customCaps.canRead)
        assertTrue(customCaps.supportsHce)
        assertFalse(customCaps.supportsMifareClassic)
    }
}
