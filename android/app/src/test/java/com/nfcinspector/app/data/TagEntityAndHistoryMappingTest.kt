package com.nfcinspector.app.data

import com.nfcinspector.app.data.local.TagEntity
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.domain.model.ReaderSourceType
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class TagEntityAndHistoryMappingTest {

    @Test
    fun testLegacyScanUuidDeterminism() {
        val legacyEntity = TagEntity(
            id = 42L,
            scanUuid = "", // Legacy record before v2 migration
            timestamp = 1773000000000L,
            readerSourceType = "ANDROID_NFC",
            readerName = "NFC Interno Android",
            readerId = "internal_android_adapter",
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologiesCsv = "NfcA,MifareClassic",
            nfcAJson = null,
            nfcBJson = null,
            isoDepJson = null,
            mifareClassicJson = null,
            mifareUltralightJson = null,
            nfcFJson = null,
            nfcVJson = null,
            ndefJson = null,
            isNdefFormatable = false,
            fullReport = ""
        )

        val expectedUuid = UUID.nameUUIDFromBytes("legacy_${legacyEntity.id}_${legacyEntity.timestamp}".toByteArray()).toString()

        // Multiple computations must produce the exact same deterministic UUID
        val comp1 = if (legacyEntity.scanUuid.isNotBlank()) legacyEntity.scanUuid else UUID.nameUUIDFromBytes("legacy_${legacyEntity.id}_${legacyEntity.timestamp}".toByteArray()).toString()
        val comp2 = if (legacyEntity.scanUuid.isNotBlank()) legacyEntity.scanUuid else UUID.nameUUIDFromBytes("legacy_${legacyEntity.id}_${legacyEntity.timestamp}".toByteArray()).toString()

        assertEquals(expectedUuid, comp1)
        assertEquals(comp1, comp2)
    }

    @Test
    fun testReaderSourcePersistenceRoundTrip() {
        // 1. Android internal NFC
        val androidSource = ReaderSource.INTERNAL_NFC
        val androidEntity = TagEntity(
            id = 1L,
            scanUuid = "uuid-1",
            timestamp = 1773000000000L,
            readerSourceType = androidSource.sourceType.name,
            readerName = androidSource.readerName,
            readerId = androidSource.readerId ?: "",
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologiesCsv = "NfcA",
            nfcAJson = null,
            nfcBJson = null,
            isoDepJson = null,
            mifareClassicJson = null,
            mifareUltralightJson = null,
            nfcFJson = null,
            nfcVJson = null,
            ndefJson = null,
            isNdefFormatable = false,
            fullReport = ""
        )

        val restoredAndroidSource = ReaderSource.fromPersisted(
            sourceTypeStr = androidEntity.readerSourceType,
            readerName = androidEntity.readerName,
            readerId = androidEntity.readerId
        )
        assertEquals(ReaderSourceType.ANDROID_NFC, restoredAndroidSource.sourceType)
        assertEquals("android_nfc", restoredAndroidSource.transport)
        assertEquals("internal_android_adapter", restoredAndroidSource.readerId)

        // 2. USB Reader with no readerId
        val usbSource = ReaderSource(
            sourceType = ReaderSourceType.USB,
            readerName = "ACR122U USB Reader",
            readerId = null,
            transport = "usb"
        )
        val usbEntity = TagEntity(
            id = 2L,
            scanUuid = "uuid-2",
            timestamp = 1773000000000L,
            readerSourceType = usbSource.sourceType.name,
            readerName = usbSource.readerName,
            readerId = usbSource.readerId ?: "",
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologiesCsv = "NfcA",
            nfcAJson = null,
            nfcBJson = null,
            isoDepJson = null,
            mifareClassicJson = null,
            mifareUltralightJson = null,
            nfcFJson = null,
            nfcVJson = null,
            ndefJson = null,
            isNdefFormatable = false,
            fullReport = ""
        )

        val restoredUsbSource = ReaderSource.fromPersisted(
            sourceTypeStr = usbEntity.readerSourceType,
            readerName = usbEntity.readerName,
            readerId = usbEntity.readerId
        )
        assertEquals(ReaderSourceType.USB, restoredUsbSource.sourceType)
        assertEquals("usb", restoredUsbSource.transport)
        assertNull("USB reader must not be given internal_android_adapter", restoredUsbSource.readerId)

        // 3. Bluetooth Reader
        val btSource = ReaderSource(
            sourceType = ReaderSourceType.BLUETOOTH,
            readerName = "ACR1255U BLE",
            readerId = "ble_001122",
            transport = "bluetooth"
        )
        val btEntity = TagEntity(
            id = 3L,
            scanUuid = "uuid-3",
            timestamp = 1773000000000L,
            readerSourceType = btSource.sourceType.name,
            readerName = btSource.readerName,
            readerId = btSource.readerId ?: "",
            uidColonHex = "04:5A:B2:1A",
            uidContinuousHex = "045AB21A",
            uidDecimal = "73052698",
            uidLengthBytes = 4,
            mainTechnology = "MIFARE Classic 1K",
            technologiesCsv = "NfcA",
            nfcAJson = null,
            nfcBJson = null,
            isoDepJson = null,
            mifareClassicJson = null,
            mifareUltralightJson = null,
            nfcFJson = null,
            nfcVJson = null,
            ndefJson = null,
            isNdefFormatable = false,
            fullReport = ""
        )

        val restoredBtSource = ReaderSource.fromPersisted(
            sourceTypeStr = btEntity.readerSourceType,
            readerName = btEntity.readerName,
            readerId = btEntity.readerId
        )
        assertEquals(ReaderSourceType.BLUETOOTH, restoredBtSource.sourceType)
        assertEquals("bluetooth", restoredBtSource.transport)
        assertEquals("ble_001122", restoredBtSource.readerId)
    }
}
