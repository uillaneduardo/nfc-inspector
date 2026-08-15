package com.nfcinspector.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val uidColonHex: String,
    val uidContinuousHex: String,
    val uidDecimal: String,
    val uidLengthBytes: Int,
    val mainTechnology: String,
    val technologiesCsv: String,
    val nfcAJson: String?,
    val nfcBJson: String?,
    val isoDepJson: String?,
    val mifareClassicJson: String?,
    val mifareUltralightJson: String?,
    val nfcFJson: String?,
    val nfcVJson: String?,
    val ndefJson: String?,
    val isNdefFormatable: Boolean,
    val fullReport: String
)
