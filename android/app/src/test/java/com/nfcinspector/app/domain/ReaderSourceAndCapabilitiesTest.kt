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
    fun testFromPersistedDoesNotAssignInternalAdapterToNonAndroidReaders() {
        // USB reader without readerId
        val usbSource = ReaderSource.fromPersisted(
            sourceTypeStr = "USB",
            readerName = "Leitor USB Externo",
            readerId = ""
        )
        assertEquals(ReaderSourceType.USB, usbSource.sourceType)
        assertEquals("usb", usbSource.transport)
        assertNull("USB reader should not receive internal_android_adapter", usbSource.readerId)

        // USB reader with "internal_android_adapter" persisted by mistake must be sanitized
        val sanitizedUsb = ReaderSource.fromPersisted(
            sourceTypeStr = "USB",
            readerName = "Leitor USB Externo",
            readerId = "internal_android_adapter"
        )
        assertNull("Non-Android reader with legacy internal_android_adapter must be sanitized to null", sanitizedUsb.readerId)
        assertEquals("usb", sanitizedUsb.transport)

        // Bluetooth reader
        val btSource = ReaderSource.fromPersisted(
            sourceTypeStr = "BLUETOOTH",
            readerName = "Leitor Bluetooth",
            readerId = "ble_dev_42"
        )
        assertEquals(ReaderSourceType.BLUETOOTH, btSource.sourceType)
        assertEquals("bluetooth", btSource.transport)
        assertEquals("ble_dev_42", btSource.readerId)

        // Remote reader
        val remoteSource = ReaderSource.fromPersisted(
            sourceTypeStr = "REMOTE",
            readerName = null,
            readerId = null
        )
        assertEquals(ReaderSourceType.REMOTE, remoteSource.sourceType)
        assertEquals("remote", remoteSource.transport)
        assertNull(remoteSource.readerId)

        // Imported reader
        val importedSource = ReaderSource.fromPersisted(
            sourceTypeStr = "IMPORTED",
            readerName = null,
            readerId = null
        )
        assertEquals(ReaderSourceType.IMPORTED, importedSource.sourceType)
        assertEquals("imported", importedSource.transport)
        assertNull(importedSource.readerId)

        // Android NFC reader
        val androidSource = ReaderSource.fromPersisted(
            sourceTypeStr = "ANDROID_NFC",
            readerName = "NFC Interno Android",
            readerId = null
        )
        assertEquals(ReaderSourceType.ANDROID_NFC, androidSource.sourceType)
        assertEquals("android_nfc", androidSource.transport)
        assertEquals("internal_android_adapter", androidSource.readerId)
    }

    @Test
    fun testAndroidInternalReaderModeBaselineCapabilities() {
        val internalCapabilities = ReaderCapabilities.ANDROID_INTERNAL_READER_MODE
        assertTrue(internalCapabilities.canRead)
        // Baseline must NOT make assumptions about hardware-dependent technologies
        assertFalse(internalCapabilities.supportsIsoDep)
        assertFalse(internalCapabilities.supportsMifareClassic)
        assertFalse(internalCapabilities.supportsIso15693)
        assertFalse(internalCapabilities.supportsFelica)
        assertFalse(internalCapabilities.supportsApdu)
        assertFalse(internalCapabilities.supportsHce) // HCE is device card emulation, not reader mode
    }

    @Test
    fun testEvidenceBasedCapabilitiesFromDetectedTechnologies() {
        val mifareScan = ReaderCapabilities.fromDetectedTechnologies(listOf("android.nfc.tech.NfcA", "android.nfc.tech.MifareClassic"))
        assertTrue(mifareScan.canRead)
        assertTrue(mifareScan.supportsMifareClassic)
        assertTrue(mifareScan.supportsRawTransceive)
        assertFalse(mifareScan.supportsIsoDep)
        assertFalse(mifareScan.supportsApdu)

        val isoDepScan = ReaderCapabilities.fromDetectedTechnologies(listOf("android.nfc.tech.IsoDep", "android.nfc.tech.NfcA"))
        assertTrue(isoDepScan.canRead)
        assertTrue(isoDepScan.supportsIsoDep)
        assertTrue(isoDepScan.supportsApdu)
        assertFalse(isoDepScan.supportsMifareClassic)

        val ndefScan = ReaderCapabilities.fromDetectedTechnologies(listOf("android.nfc.tech.NfcV", "android.nfc.tech.Ndef"))
        assertTrue(ndefScan.canRead)
        assertTrue(ndefScan.supportsNdef)
        assertTrue(ndefScan.supportsIso15693)
    }
}
