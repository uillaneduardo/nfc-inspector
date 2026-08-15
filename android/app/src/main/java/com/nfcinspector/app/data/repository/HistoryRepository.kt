package com.nfcinspector.app.data.repository

import com.nfcinspector.app.data.local.TagDao
import com.nfcinspector.app.data.local.TagEntity
import com.nfcinspector.app.data.model.TagRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(private val tagDao: TagDao) {

    val allScans: Flow<List<TagRecord>> = tagDao.getAllTags().map { entities ->
        entities.map { entity ->
            TagRecord(
                id = entity.id,
                timestamp = entity.timestamp,
                uidColonHex = entity.uidColonHex,
                uidContinuousHex = entity.uidContinuousHex,
                uidDecimal = entity.uidDecimal,
                uidLengthBytes = entity.uidLengthBytes,
                mainTechnology = entity.mainTechnology,
                technologies = if (entity.technologiesCsv.isNotBlank()) entity.technologiesCsv.split(",") else emptyList(),
                isNdefFormatable = entity.isNdefFormatable
            )
        }
    }

    suspend fun saveScan(record: TagRecord): Long {
        val entity = TagEntity(
            timestamp = record.timestamp,
            uidColonHex = record.uidColonHex,
            uidContinuousHex = record.uidContinuousHex,
            uidDecimal = record.uidDecimal,
            uidLengthBytes = record.uidLengthBytes,
            mainTechnology = record.mainTechnology,
            technologiesCsv = record.technologies.joinToString(","),
            nfcAJson = null,
            nfcBJson = null,
            isoDepJson = null,
            mifareClassicJson = null,
            mifareUltralightJson = null,
            nfcFJson = null,
            nfcVJson = null,
            ndefJson = null,
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
}
