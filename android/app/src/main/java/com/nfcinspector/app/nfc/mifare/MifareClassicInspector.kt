package com.nfcinspector.app.nfc.mifare

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import com.nfcinspector.app.data.model.MifareBlockData
import com.nfcinspector.app.data.model.MifareBlockType
import com.nfcinspector.app.data.model.MifareClassicMemoryMap
import com.nfcinspector.app.data.model.MifareSectorData
import com.nfcinspector.app.data.model.MifareSectorStatus
import java.io.IOException
import java.util.Locale

/**
 * Diagnostic and inspection engine for MIFARE Classic tags.
 *
 * Performs safe, read-only analysis of sectors and memory blocks without altering tag content.
 * Strictly 100% offline.
 */
object MifareClassicInspector {

    /**
     * Standard public diagnostic keys used for diagnostic/authorized tag analysis.
     */
    val DIAGNOSTIC_KEYS: List<Pair<String, ByteArray>> = listOf(
        "Padrão Transporte (FF..FF)" to MifareClassic.KEY_DEFAULT, // FFFFFFFFFFFF
        "NFC Forum (D3..F7)" to MifareClassic.KEY_NFC_FORUM, // D3F7D3F7D3F7
        "MAD / NXP (A0..A5)" to MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY, // A0A1A2A3A4A5
        "Zeros (00..00)" to byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
        "Padrão A0..F0" to byteArrayOf(0xA0.toByte(), 0xB0.toByte(), 0xC0.toByte(), 0xD0.toByte(), 0xE0.toByte(), 0xF0.toByte()),
        "Padrão B0..B5" to byteArrayOf(0xB0.toByte(), 0xB1.toByte(), 0xB2.toByte(), 0xB3.toByte(), 0xB4.toByte(), 0xB5.toByte()),
        "NDEF Key 1" to byteArrayOf(0x4D.toByte(), 0x3A.toByte(), 0x99.toByte(), 0xC3.toByte(), 0x51.toByte(), 0xDD.toByte()),
        "NDEF Key 2" to byteArrayOf(0x1A.toByte(), 0x98.toByte(), 0x2C.toByte(), 0x7E.toByte(), 0x45.toByte(), 0x9A.toByte())
    )

    /**
     * Builds the baseline structural memory map without performing RF communication.
     */
    fun buildInitialStructure(mfc: MifareClassic): MifareClassicMemoryMap {
        val typeStr = getTypeName(mfc.type)
        val sectorCount = mfc.sectorCount
        val totalBlockCount = mfc.blockCount
        val sizeBytes = mfc.size

        val sectorsList = mutableListOf<MifareSectorData>()

        for (s in 0 until sectorCount) {
            val blockCountInSector = mfc.getBlockCountInSector(s)
            val firstBlock = mfc.sectorToBlock(s)

            val blocksList = (0 until blockCountInSector).map { bOffset ->
                val absBlock = firstBlock + bOffset
                val bType = when {
                    s == 0 && bOffset == 0 -> MifareBlockType.MANUFACTURER
                    bOffset == blockCountInSector - 1 -> MifareBlockType.SECTOR_TRAILER
                    else -> MifareBlockType.DATA
                }
                MifareBlockData(
                    blockIndex = absBlock,
                    blockIndexInSector = bOffset,
                    blockType = bType,
                    rawBytes = null,
                    hexFormatted = "Não lido / Protegido",
                    asciiFormatted = "—",
                    isReadSuccess = false
                )
            }

            sectorsList.add(
                MifareSectorData(
                    sectorIndex = s,
                    blockCount = blockCountInSector,
                    firstBlockIndex = firstBlock,
                    status = MifareSectorStatus.NOT_TESTED,
                    blocks = blocksList
                )
            )
        }

        return MifareClassicMemoryMap(
            typeName = typeStr,
            sizeBytes = sizeBytes,
            sectorCount = sectorCount,
            blockCount = totalBlockCount,
            blockSizeBytes = MifareClassic.BLOCK_SIZE,
            sectors = sectorsList,
            isInspected = false
        )
    }

    /**
     * Inspects and attempts authentication of MIFARE Classic sectors using standard diagnostic keys
     * and optional user-provided keys.
     */
    fun inspectMifare(
        tag: Tag,
        customKeyA: ByteArray? = null,
        customKeyB: ByteArray? = null,
        testDefaultKeys: Boolean = true
    ): MifareClassicMemoryMap? {
        val mfc = MifareClassic.get(tag) ?: return null

        val baseStructure = buildInitialStructure(mfc)
        val sectorCount = mfc.sectorCount

        var authenticatedSectors = 0
        var fullyReadSectors = 0
        var totalBlocksRead = 0

        val inspectedSectors = mutableListOf<MifareSectorData>()

        try {
            if (!mfc.isConnected) {
                mfc.connect()
            }

            // Build list of keys to test
            val keysToTestA = mutableListOf<Pair<String, ByteArray>>()
            val keysToTestB = mutableListOf<Pair<String, ByteArray>>()

            if (customKeyA != null && customKeyA.size == 6) {
                keysToTestA.add("Key A (Personalizada)" to customKeyA)
            }
            if (customKeyB != null && customKeyB.size == 6) {
                keysToTestB.add("Key B (Personalizada)" to customKeyB)
            }

            if (testDefaultKeys) {
                keysToTestA.addAll(DIAGNOSTIC_KEYS)
                keysToTestB.addAll(DIAGNOSTIC_KEYS)
            }

            for (s in 0 until sectorCount) {
                val blockCountInSector = mfc.getBlockCountInSector(s)
                val firstBlock = mfc.sectorToBlock(s)
                var sectorStatus = MifareSectorStatus.AUTH_FAILED
                var authKeyDesc: String? = null
                var authKeyHex: String? = null

                var authenticated = false

                // Try Key A candidates
                for ((desc, keyBytes) in keysToTestA) {
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, keyBytes)) {
                            authenticated = true
                            sectorStatus = MifareSectorStatus.AUTH_KEY_A
                            authKeyDesc = desc
                            authKeyHex = toHex(keyBytes)
                            break
                        }
                    } catch (_: Exception) {
                        // Keep trying next key or handle tag disconnect
                    }
                }

                // If not authenticated with Key A, try Key B candidates
                if (!authenticated) {
                    for ((desc, keyBytes) in keysToTestB) {
                        try {
                            if (mfc.authenticateSectorWithKeyB(s, keyBytes)) {
                                authenticated = true
                                sectorStatus = MifareSectorStatus.AUTH_KEY_B
                                authKeyDesc = desc
                                authKeyHex = toHex(keyBytes)
                                break
                            }
                        } catch (_: Exception) {
                            // Keep trying next key
                        }
                    }
                }

                val sectorBlocks = mutableListOf<MifareBlockData>()
                var sectorBlocksRead = 0
                var trailerBytes: ByteArray? = null

                if (authenticated) {
                    authenticatedSectors++

                    for (bOffset in 0 until blockCountInSector) {
                        val absBlock = firstBlock + bOffset
                        val bType = when {
                            s == 0 && bOffset == 0 -> MifareBlockType.MANUFACTURER
                            bOffset == blockCountInSector - 1 -> MifareBlockType.SECTOR_TRAILER
                            else -> MifareBlockType.DATA
                        }

                        try {
                            val blockBytes = mfc.readBlock(absBlock)
                            sectorBlocksRead++
                            totalBlocksRead++

                            if (bType == MifareBlockType.SECTOR_TRAILER) {
                                trailerBytes = blockBytes
                            }

                            sectorBlocks.add(
                                MifareBlockData(
                                    blockIndex = absBlock,
                                    blockIndexInSector = bOffset,
                                    blockType = bType,
                                    rawBytes = blockBytes,
                                    hexFormatted = formatHexSpaced(blockBytes),
                                    asciiFormatted = formatAsciiSafe(blockBytes),
                                    isReadSuccess = true
                                )
                            )
                        } catch (e: Exception) {
                            sectorBlocks.add(
                                MifareBlockData(
                                    blockIndex = absBlock,
                                    blockIndexInSector = bOffset,
                                    blockType = bType,
                                    rawBytes = null,
                                    hexFormatted = "Não autorizado / Erro de leitura",
                                    asciiFormatted = "—",
                                    isReadSuccess = false,
                                    readError = e.localizedMessage ?: "Erro na leitura do bloco"
                                )
                            )
                        }
                    }

                    if (sectorBlocksRead == blockCountInSector) {
                        sectorStatus = MifareSectorStatus.READ_SUCCESS
                        fullyReadSectors++
                    } else if (sectorBlocksRead > 0) {
                        sectorStatus = MifareSectorStatus.PARTIAL_READ
                    }
                } else {
                    // Not authenticated: preserve structural map with unread blocks
                    for (bOffset in 0 until blockCountInSector) {
                        val absBlock = firstBlock + bOffset
                        val bType = when {
                            s == 0 && bOffset == 0 -> MifareBlockType.MANUFACTURER
                            bOffset == blockCountInSector - 1 -> MifareBlockType.SECTOR_TRAILER
                            else -> MifareBlockType.DATA
                        }
                        sectorBlocks.add(
                            MifareBlockData(
                                blockIndex = absBlock,
                                blockIndexInSector = bOffset,
                                blockType = bType,
                                rawBytes = null,
                                hexFormatted = "Não autenticado / Chave desconhecida",
                                asciiFormatted = "—",
                                isReadSuccess = false
                            )
                        )
                    }
                }

                // Parse Access Bits from sector trailer if read
                val accessBits = trailerBytes?.let { tb ->
                    if (tb.size >= 10) {
                        val abBytes = byteArrayOf(tb[6], tb[7], tb[8])
                        val gpb = tb[9]
                        MifareAccessBitsParser.parse(abBytes, gpb)
                    } else null
                }

                inspectedSectors.add(
                    MifareSectorData(
                        sectorIndex = s,
                        blockCount = blockCountInSector,
                        firstBlockIndex = firstBlock,
                        status = sectorStatus,
                        authKeyType = authKeyDesc,
                        authKeyUsedHex = authKeyHex,
                        blocks = sectorBlocks,
                        accessBits = accessBits
                    )
                )
            }

            return baseStructure.copy(
                sectors = inspectedSectors,
                isInspected = true,
                authenticatedSectorsCount = authenticatedSectors,
                fullyReadSectorsCount = fullyReadSectors,
                totalBlocksReadCount = totalBlocksRead
            )

        } catch (e: Exception) {
            // If tag removed or connection failed midway, return whatever partial map was built
            return if (inspectedSectors.isNotEmpty()) {
                baseStructure.copy(
                    sectors = inspectedSectors,
                    isInspected = true,
                    authenticatedSectorsCount = authenticatedSectors,
                    fullyReadSectorsCount = fullyReadSectors,
                    totalBlocksReadCount = totalBlocksRead
                )
            } else {
                baseStructure
            }
        } finally {
            try {
                if (mfc.isConnected) {
                    mfc.close()
                }
            } catch (_: Exception) {}
        }
    }

    private fun getTypeName(type: Int): String {
        return when (type) {
            MifareClassic.TYPE_CLASSIC -> "MIFARE Classic Standard"
            MifareClassic.TYPE_PLUS -> "MIFARE Plus (SL1 emulado)"
            MifareClassic.TYPE_PRO -> "MIFARE Pro"
            else -> "MIFARE Desconhecido"
        }
    }

    fun formatHexSpaced(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return "—"
        return bytes.joinToString(" ") { String.format(Locale.US, "%02X", it) }
    }

    fun formatAsciiSafe(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return "—"
        val sb = StringBuilder()
        for (b in bytes) {
            val c = b.toInt() and 0xFF
            if (c in 32..126) {
                sb.append(c.toChar())
            } else {
                sb.append('.')
            }
        }
        return sb.toString()
    }

    private fun toHex(bytes: ByteArray): String {
        return bytes.joinToString("") { String.format(Locale.US, "%02X", it) }
    }

    fun parseHexKey(hex: String): ByteArray? {
        val clean = hex.replace(" ", "").replace(":", "").uppercase(Locale.US)
        if (clean.length != 12) return null
        return try {
            val result = ByteArray(6)
            for (i in 0 until 6) {
                val byteStr = clean.substring(i * 2, i * 2 + 2)
                result[i] = byteStr.toInt(16).toByte()
            }
            result
        } catch (_: Exception) {
            null
        }
    }
}
