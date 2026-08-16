package com.nfcinspector.app.data.repository

import com.nfcinspector.app.data.local.TagDao
import com.nfcinspector.app.data.local.TagEntity
import com.nfcinspector.app.data.local.TechSerializer
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.domain.model.ReaderSource
import com.nfcinspector.app.domain.model.ReaderSourceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class HistoryRepository(private val tagDao: TagDao) {

    val allScans: Flow<List<TagRecord>> = tagDao.getAllTags().map { entities ->
        entities.map { entity -> mapEntityToRecord(entity) }
    }

    suspend fun getScanById(id: Long): TagRecord? {
        val entity = tagDao.getTagById(id) ?: return null
        return mapEntityToRecord(entity)
    }

    suspend fun saveScan(record: TagRecord): Long {
        val entity = TagEntity(
            id = record.id,
            scanUuid = record.scanId,
            timestamp = record.timestamp,
            readerSourceType = record.readerSource.sourceType.name,
            readerName = record.readerSource.readerName,
            readerId = record.readerSource.readerId ?: "internal_android_adapter",
            uidColonHex = record.uidColonHex,
            uidContinuousHex = record.uidContinuousHex,
            uidDecimal = record.uidDecimal,
            uidLengthBytes = record.uidLengthBytes,
            mainTechnology = record.mainTechnology,
            technologiesCsv = record.technologies.joinToString(","),
            nfcAJson = TechSerializer.serializeNfcA(record.nfcA),
            nfcBJson = TechSerializer.serializeNfcB(record.nfcB),
            isoDepJson = TechSerializer.serializeIsoDep(record.isoDep),
            mifareClassicJson = TechSerializer.serializeMifareClassic(record.mifareClassic),
            mifareUltralightJson = TechSerializer.serializeMifareUltralight(record.mifareUltralight),
            nfcFJson = TechSerializer.serializeNfcF(record.nfcF),
            nfcVJson = TechSerializer.serializeNfcV(record.nfcV),
            ndefJson = TechSerializer.serializeNdef(record.ndef),
            isNdefFormatable = record.isNdefFormatable,
            fullReport = record.generateFullReport()
        )
        return tagDao.insertTag(entity)
    }

    suspend fun deleteScan(id: Long) {
        tagDao.deleteTagById(id)
    }

    suspend fun deleteAllScans() {
        tagDao.deleteAllTags()
    }

    private fun mapEntityToRecord(entity: TagEntity): TagRecord {
        // Safe backward-compatible fallback for scanId: if empty on legacy records, generate deterministic UUID
        val stableScanId = if (entity.scanUuid.isNotBlank()) {
            entity.scanUuid
        } else {
            UUID.nameUUIDFromBytes("legacy_${entity.id}_${entity.timestamp}".toByteArray()).toString()
        }

        val readerSource = ReaderSource(
            sourceType = ReaderSourceType.fromDbString(entity.readerSourceType),
            readerName = entity.readerName.ifBlank { "NFC Interno Android" },
            readerId = entity.readerId.ifBlank { "internal_android_adapter" },
            transport = if (entity.readerSourceType == "USB") "usb" else "android_nfc"
        )

        return TagRecord(
            id = entity.id,
            scanId = stableScanId,
            timestamp = entity.timestamp,
            readerSource = readerSource,
            uidColonHex = entity.uidColonHex,
            uidContinuousHex = entity.uidContinuousHex,
            uidDecimal = entity.uidDecimal,
            uidLengthBytes = entity.uidLengthBytes,
            mainTechnology = entity.mainTechnology,
            technologies = if (entity.technologiesCsv.isNotBlank()) entity.technologiesCsv.split(",") else emptyList(),
            nfcA = TechSerializer.deserializeNfcA(entity.nfcAJson),
            nfcB = TechSerializer.deserializeNfcB(entity.nfcBJson),
            isoDep = TechSerializer.deserializeIsoDep(entity.isoDepJson),
            mifareClassic = TechSerializer.deserializeMifareClassic(entity.mifareClassicJson),
            mifareUltralight = TechSerializer.deserializeMifareUltralight(entity.mifareUltralightJson),
            nfcF = TechSerializer.deserializeNfcF(entity.nfcFJson),
            nfcV = TechSerializer.deserializeNfcV(entity.nfcVJson),
            ndef = TechSerializer.deserializeNdef(entity.ndefJson),
            isNdefFormatable = entity.isNdefFormatable
        )
    }
}


