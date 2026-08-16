package com.nfcinspector.app.nfc.mifare

import com.nfcinspector.app.data.model.BlockAccessPermissions
import com.nfcinspector.app.data.model.MifareAccessBits
import com.nfcinspector.app.data.model.TrailerAccessPermissions
import java.util.Locale

/**
 * Isolated, testable parser for MIFARE Classic Access Bits.
 *
 * Receives the 3 access bytes (Bytes 6, 7, 8 of the Sector Trailer) and optional GPB (Byte 9).
 * Decodes C1, C2, C3 bits for all blocks/groups in the sector and computes read/write permissions.
 *
 * Supports both standard 4-block sectors (MIFARE Classic 1K / 4K sectors 0..31)
 * and large 16-block sectors (MIFARE Classic 4K sectors 32..39).
 */
object MifareAccessBitsParser {

    /**
     * Parses the 3 access bytes and validates the complementary inverted bits.
     *
     * @param accessBytes ByteArray of length 3 (Byte 6, 7, 8 of Sector Trailer) or length >= 4 (including GPB).
     * @param gpb Optional GPB (General Purpose Byte / Byte 9).
     * @param blockCountInSector Number of blocks in this sector (4 for standard sectors, 16 for large 4K sectors).
     * @return Decoded [MifareAccessBits] structure.
     */
    fun parse(
        accessBytes: ByteArray,
        gpb: Byte? = null,
        blockCountInSector: Int = 4
    ): MifareAccessBits {
        if (accessBytes.size < 3) {
            return MifareAccessBits(
                rawBytesHex = accessBytes.joinToString("") { String.format(Locale.US, "%02X", it) },
                gpbHex = gpb?.let { String.format(Locale.US, "0x%02X", it) },
                isValid = false,
                inconsistencyError = "Tamanho insuficiente para bytes de acesso (< 3 bytes)."
            )
        }

        val b6 = accessBytes[0].toInt() and 0xFF
        val b7 = accessBytes[1].toInt() and 0xFF
        val b8 = accessBytes[2].toInt() and 0xFF

        val gpbValue = gpb ?: if (accessBytes.size >= 4) accessBytes[3] else null
        val gpbHex = gpbValue?.let { String.format(Locale.US, "0x%02X", it) }
        val rawHex = String.format(Locale.US, "%02X %02X %02X", b6, b7, b8)

        // Validate complementary bits for each of the 4 block access groups (0, 1, 2, and sector trailer 3)
        val c1 = IntArray(4)
        val c2 = IntArray(4)
        val c3 = IntArray(4)
        val validationErrors = mutableListOf<String>()

        for (b in 0..3) {
            val notC1 = (b6 ushr b) and 1
            val c1Bit = (b7 ushr (4 + b)) and 1

            val notC2 = (b6 ushr (4 + b)) and 1
            val c2Bit = (b8 ushr b) and 1

            val notC3 = (b7 ushr b) and 1
            val c3Bit = (b8 ushr (4 + b)) and 1

            if ((c1Bit xor notC1) != 1) {
                validationErrors.add("Inconsistência de C1 para grupo $b (C1=$c1Bit, ~C1=$notC1)")
            }
            if ((c2Bit xor notC2) != 1) {
                validationErrors.add("Inconsistência de C2 para grupo $b (C2=$c2Bit, ~C2=$notC2)")
            }
            if ((c3Bit xor notC3) != 1) {
                validationErrors.add("Inconsistência de C3 para grupo $b (C3=$c3Bit, ~C3=$notC3)")
            }

            c1[b] = c1Bit
            c2[b] = c2Bit
            c3[b] = c3Bit
        }

        if (validationErrors.isNotEmpty()) {
            return MifareAccessBits(
                rawBytesHex = rawHex,
                gpbHex = gpbHex,
                isValid = false,
                inconsistencyError = "Bits de acesso corrompidos ou inconsistentes: " + validationErrors.joinToString("; ")
            )
        }

        // Calculate Data Block/Group permissions (Groups 0, 1, 2)
        val isLargeSector = blockCountInSector > 4
        val blockPermissionsList = (0..2).map { groupIdx ->
            val label = when {
                isLargeSector && groupIdx == 0 -> "Blocos 0..4 (Grupo 0)"
                isLargeSector && groupIdx == 1 -> "Blocos 5..9 (Grupo 1)"
                isLargeSector && groupIdx == 2 -> "Blocos 10..14 (Grupo 2)"
                else -> "Bloco $groupIdx"
            }
            evaluateDataBlockPermissions(
                blockRangeLabel = label,
                groupIndex = groupIdx,
                c1 = c1[groupIdx],
                c2 = c2[groupIdx],
                c3 = c3[groupIdx]
            )
        }

        // Calculate Sector Trailer permissions (Group 3 / last block)
        val trailerPermissions = evaluateTrailerPermissions(
            c1 = c1[3],
            c2 = c2[3],
            c3 = c3[3]
        )

        return MifareAccessBits(
            rawBytesHex = rawHex,
            gpbHex = gpbHex,
            isValid = true,
            inconsistencyError = null,
            blockPermissions = blockPermissionsList,
            trailerPermissions = trailerPermissions
        )
    }

    /**
     * Evaluates data block access conditions according to MIFARE Classic 1K/4K specification.
     */
    fun evaluateDataBlockPermissions(
        blockRangeLabel: String,
        groupIndex: Int,
        c1: Int,
        c2: Int,
        c3: Int
    ): BlockAccessPermissions {
        val pattern = "$c1$c2$c3"
        val (read, write, inc, dec) = when (pattern) {
            "000" -> Quad("Key A|B", "Key A|B", "Key A|B", "Key A|B (Padrão Transporte)")
            "010" -> Quad("Key A|B", "Nunca", "Nunca", "Nunca (Somente Leitura)")
            "100" -> Quad("Key A|B", "Key B", "Nunca", "Nunca")
            "110" -> Quad("Key A|B", "Key B", "Key B", "Key A|B (Bloco de Valor)")
            "001" -> Quad("Key A|B", "Nunca", "Nunca", "Key A|B (Bloco de Valor)")
            "011" -> Quad("Key B", "Key B", "Nunca", "Nunca")
            "101" -> Quad("Key B", "Nunca", "Nunca", "Nunca")
            "111" -> Quad("Nunca", "Nunca", "Nunca", "Nunca (Bloqueado)")
            else -> Quad("Desconhecido", "Desconhecido", "Desconhecido", "Desconhecido")
        }

        return BlockAccessPermissions(
            blockRangeLabel = blockRangeLabel,
            groupIndex = groupIndex,
            c1 = c1,
            c2 = c2,
            c3 = c3,
            readAccess = read,
            writeAccess = write,
            incrementAccess = inc,
            decrementTransferRestoreAccess = dec
        )
    }

    /**
     * Evaluates Sector Trailer access conditions according to MIFARE Classic 1K/4K specification.
     */
    fun evaluateTrailerPermissions(
        c1: Int,
        c2: Int,
        c3: Int
    ): TrailerAccessPermissions {
        val pattern = "$c1$c2$c3"
        val (keyAWrite, accessBitsRead, accessBitsWrite, keyBRead, keyBWrite) = when (pattern) {
            "000" -> Quint("Key A", "Key A", "Nunca", "Key A (Legível)", "Key A")
            "010" -> Quint("Nunca", "Key A", "Nunca", "Key A (Legível)", "Nunca")
            "100" -> Quint("Key B", "Key A|B", "Key B", "Nunca (Auth)", "Key B")
            "110" -> Quint("Nunca", "Key A|B", "Nunca", "Nunca (Auth)", "Nunca")
            "001" -> Quint("Key A", "Key A", "Key A", "Key A (Legível)", "Key A")
            "011" -> Quint("Key B", "Key A|B", "Key B", "Nunca (Auth)", "Key B")
            "101" -> Quint("Nunca", "Key A|B", "Nunca", "Nunca (Auth)", "Nunca")
            "111" -> Quint("Nunca", "Key A|B", "Nunca", "Nunca (Auth)", "Nunca")
            else -> Quint("Desconhecido", "Desconhecido", "Desconhecido", "Desconhecido", "Desconhecido")
        }

        return TrailerAccessPermissions(
            c1 = c1,
            c2 = c2,
            c3 = c3,
            keyARead = "Nunca (Protegido por HW)",
            keyAWrite = keyAWrite,
            accessBitsRead = accessBitsRead,
            accessBitsWrite = accessBitsWrite,
            keyBRead = keyBRead,
            keyBWrite = keyBWrite
        )
    }

    private data class Quad(val first: String, val second: String, val third: String, val fourth: String)
    private data class Quint(val a: String, val b: String, val c: String, val d: String, val e: String)
}

