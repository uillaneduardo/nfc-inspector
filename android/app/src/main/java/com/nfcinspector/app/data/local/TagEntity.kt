package com.nfcinspector.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "scanUuid", defaultValue = "")
    val scanUuid: String = "",
    val timestamp: Long,
    @ColumnInfo(name = "readerSourceType", defaultValue = "ANDROID_NFC")
    val readerSourceType: String = "ANDROID_NFC",
    @ColumnInfo(name = "readerName", defaultValue = "NFC Interno Android")
    val readerName: String = "NFC Interno Android",
    @ColumnInfo(name = "readerId", defaultValue = "internal_android_adapter")
    val readerId: String = "internal_android_adapter",
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

