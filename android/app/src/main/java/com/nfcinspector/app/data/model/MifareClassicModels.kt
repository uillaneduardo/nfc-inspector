package com.nfcinspector.app.data.model

/**
 * Structural type of a MIFARE Classic block.
 */
enum class MifareBlockType(val label: String) {
    MANUFACTURER("Manufacturer Block"),
    DATA("Data Block"),
    SECTOR_TRAILER("Sector Trailer")
}

/**
 * Inspection and authentication status for a specific MIFARE Classic sector.
 */
enum class MifareSectorStatus(val label: String) {
    NOT_TESTED("Não testado"),
    AUTH_FAILED("Autenticação falhou"),
    AUTH_KEY_A("Autenticado com Key A"),
    AUTH_KEY_B("Autenticado com Key B"),
    PARTIAL_READ("Leitura parcial"),
    READ_SUCCESS("Lido com sucesso")
}

/**
 * Represents access permissions for standard data blocks in a sector.
 */
data class BlockAccessPermissions(
    val blockIndexInSector: Int,
    val c1: Int,
    val c2: Int,
    val c3: Int,
    val readAccess: String,
    val writeAccess: String,
    val incrementAccess: String,
    val decrementTransferRestoreAccess: String
)

/**
 * Represents access permissions for the Sector Trailer block (block 3 or last block).
 */
data class TrailerAccessPermissions(
    val c1: Int,
    val c2: Int,
    val c3: Int,
    val keyARead: String = "Nunca (Protegido por HW)",
    val keyAWrite: String,
    val accessBitsRead: String,
    val accessBitsWrite: String,
    val keyBRead: String,
    val keyBWrite: String
)

/**
 * Decoded and validated Access Bits (Bytes 6, 7, 8 of Sector Trailer) + GPB (Byte 9).
 */
data class MifareAccessBits(
    val rawBytesHex: String,
    val gpbHex: String?,
    val isValid: Boolean,
    val inconsistencyError: String? = null,
    val blockPermissions: List<BlockAccessPermissions> = emptyList(),
    val trailerPermissions: TrailerAccessPermissions? = null
)

/**
 * Represents a single block in a MIFARE Classic memory map.
 */
data class MifareBlockData(
    val blockIndex: Int,
    val blockIndexInSector: Int,
    val blockType: MifareBlockType,
    val rawBytes: ByteArray? = null,
    val hexFormatted: String = "Não lido / Protegido",
    val asciiFormatted: String = "—",
    val isReadSuccess: Boolean = false,
    val readError: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MifareBlockData
        if (blockIndex != other.blockIndex) return false
        if (blockType != other.blockType) return false
        if (rawBytes != null) {
            if (other.rawBytes == null) return false
            if (!rawBytes.contentEquals(other.rawBytes)) return false
        } else if (other.rawBytes != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = blockIndex
        result = 31 * result + blockType.hashCode()
        result = 31 * result + (rawBytes?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Represents an entire sector with its status, authentication record, and blocks.
 */
data class MifareSectorData(
    val sectorIndex: Int,
    val blockCount: Int,
    val firstBlockIndex: Int,
    val status: MifareSectorStatus = MifareSectorStatus.NOT_TESTED,
    val authKeyType: String? = null, // "Key A", "Key B", etc.
    val authKeyUsedHex: String? = null, // Hex representation of tested key that matched
    val blocks: List<MifareBlockData> = emptyList(),
    val accessBits: MifareAccessBits? = null
)

/**
 * Detailed technical memory map of a MIFARE Classic tag.
 */
data class MifareClassicMemoryMap(
    val typeName: String,
    val sizeBytes: Int,
    val sectorCount: Int,
    val blockCount: Int,
    val blockSizeBytes: Int = 16,
    val sectors: List<MifareSectorData> = emptyList(),
    val isInspected: Boolean = false,
    val authenticatedSectorsCount: Int = 0,
    val fullyReadSectorsCount: Int = 0,
    val totalBlocksReadCount: Int = 0
)
