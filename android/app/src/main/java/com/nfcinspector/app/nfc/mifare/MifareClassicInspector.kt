package com.nfcinspector.app.nfc.mifare

import android.nfc.Tag
import android.nfc.TagLostException
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

    data class DiagnosticKey(
        val keyTypeTarget: String, // "Key A", "Key B", "Ambas"
        val name: String,
        val keyBytes: ByteArray
    ) {
        val hexFormatted: String
            get() = keyBytes.joinToString("") { String.format(Locale.US, "%02X", it) }
    }

    /**
     * Standard public diagnostic keys used for diagnostic/authorized tag analysis.
     * Restricted to official factory, MAD, NFC Forum, and null standards.
     */
    val STANDARD_DIAGNOSTIC_KEYS: List<DiagnosticKey> = listOf(
        DiagnosticKey(
            keyTypeTarget = "Ambas",
            name = "Padrão de Fábrica / Transporte (NXP)",
            keyBytes = MifareClassic.KEY_DEFAULT // FF FF FF FF FF FF
        ),
        DiagnosticKey(
            keyTypeTarget = "Key A",
            name = "NXP MAD (Diretório de Aplicações)",
            keyBytes = MifareClassic.KEY_MIFARE_APPLICATION_DIRECTORY // A0 A1 A2 A3 A4 A5
        ),
        DiagnosticKey(
            keyTypeTarget = "Ambas",
            name = "NFC Forum NDEF",
            keyBytes = MifareClassic.KEY_NFC_FORUM // D3 F7 D3 F7 D3 F7
        ),
        DiagnosticKey(
            keyTypeTarget = "Ambas",
            name = "Chave Nula (Zeros)",
            keyBytes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        )
    )

    /**
     * Resolves the descriptive variant name for MIFARE Classic tags based on native hardware size and sectors.
     */
    fun resolveVariantTypeName(type: Int, sizeBytes: Int, sectorCount: Int): String {
        return when (type) {
            MifareClassic.TYPE_CLASSIC -> when {
                sizeBytes == 320 || sectorCount == 5 -> "MIFARE Classic Mini"
                sizeBytes == 1024 || sectorCount == 16 -> "MIFARE Classic 1K"
                sizeBytes == 2048 || sectorCount == 32 -> "MIFARE Classic 2K"
                sizeBytes == 4096 || sectorCount == 40 -> "MIFARE Classic 4K"
                else -> "MIFARE Classic"
            }
            MifareClassic.TYPE_PLUS -> "MIFARE Plus (SL1 emulado)"
            MifareClassic.TYPE_PRO -> "MIFARE Pro"
            else -> "MIFARE Classic"
        }
    }

    /**
     * Builds the baseline structural memory map without performing RF communication.
     */
    fun buildInitialStructure(mfc: MifareClassic): MifareClassicMemoryMap {
        val sectorCount = mfc.sectorCount
        val totalBlockCount = mfc.blockCount
        val sizeBytes = mfc.size
        val typeStr = resolveVariantTypeName(mfc.type, sizeBytes, sectorCount)

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
     *
     * Handles TagLostException gracefully and keeps unprocessed sectors as NOT_TESTED.
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

        val inspectedSectorsMap = mutableMapOf<Int, MifareSectorData>()

        try {
            if (!mfc.isConnected) {
                mfc.connect()
            }

            // Build key lists for Key A and Key B
            val keysToTestA = mutableListOf<Pair<String, ByteArray>>()
            val keysToTestB = mutableListOf<Pair<String, ByteArray>>()

            if (customKeyA != null && customKeyA.size == 6) {
                keysToTestA.add("Chave Personalizada Key A" to customKeyA)
            }
            if (customKeyB != null && customKeyB.size == 6) {
                keysToTestB.add("Chave Personalizada Key B" to customKeyB)
            }

            if (testDefaultKeys) {
                STANDARD_DIAGNOSTIC_KEYS.forEach { dk ->
                    if (dk.keyTypeTarget == "Key A" || dk.keyTypeTarget == "Ambas") {
                        keysToTestA.add(dk.name to dk.keyBytes)
                    }
                    if (dk.keyTypeTarget == "Key B" || dk.keyTypeTarget == "Ambas") {
                        keysToTestB.add(dk.name to dk.keyBytes)
                    }
                }
            }

            for (s in 0 until sectorCount) {
                val blockCountInSector = mfc.getBlockCountInSector(s)
                val firstBlock = mfc.sectorToBlock(s)
                var sectorStatus = MifareSectorStatus.AUTH_FAILED
                var authKeyType: String? = null
                var authKeyName: String? = null
                var authKeyHex: String? = null

                var authenticated = false

                // Try Key A candidates
                for ((name, keyBytes) in keysToTestA) {
                    try {
                        if (mfc.authenticateSectorWithKeyA(s, keyBytes)) {
                            authenticated = true
                            sectorStatus = MifareSectorStatus.AUTH_KEY_A
                            authKeyType = "Key A"
                            authKeyName = name
                            authKeyHex = toHex(keyBytes)
                            break
                        }
                    } catch (tle: TagLostException) {
                        throw tle
                    } catch (_: Exception) {
                        // Try next key candidate
                    }
                }

                // If not authenticated with Key A, try Key B candidates
                if (!authenticated) {
                    for ((name, keyBytes) in keysToTestB) {
                        try {
                            if (mfc.authenticateSectorWithKeyB(s, keyBytes)) {
                                authenticated = true
                                sectorStatus = MifareSectorStatus.AUTH_KEY_B
                                authKeyType = "Key B"
                                authKeyName = name
                                authKeyHex = toHex(keyBytes)
                                break
                            }
                        } catch (tle: TagLostException) {
                            throw tle
                        } catch (_: Exception) {
                            // Try next key candidate
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
                        } catch (tle: TagLostException) {
                            throw tle
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
                    // Not authenticated: preserve unread block placeholders
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
                        MifareAccessBitsParser.parse(abBytes, gpb, blockCountInSector)
                    } else null
                }

                inspectedSectorsMap[s] = MifareSectorData(
                    sectorIndex = s,
                    blockCount = blockCountInSector,
                    firstBlockIndex = firstBlock,
                    status = sectorStatus,
                    authKeyType = authKeyType,
                    authKeyName = authKeyName,
                    authKeyUsedHex = authKeyHex,
                    blocks = sectorBlocks,
                    accessBits = accessBits
                )
            }

        } catch (tle: TagLostException) {
            // Tag was lost/removed from RF field during inspection: retain partial results
        } catch (_: IOException) {
            // IO error during inspection: retain partial results
        } catch (_: Exception) {
            // General exception: retain partial results
        } finally {
            try {
                if (mfc.isConnected) {
                    mfc.close()
                }
            } catch (_: Exception) {}
        }

        // Merge inspected sectors with uninspected (NOT_TESTED) sectors
        val finalSectors = baseStructure.sectors.map { defaultSector ->
            inspectedSectorsMap[defaultSector.sectorIndex] ?: defaultSector
        }

        return baseStructure.copy(
            sectors = finalSectors,
            isInspected = inspectedSectorsMap.isNotEmpty(),
            authenticatedSectorsCount = authenticatedSectors,
            fullyReadSectorsCount = fullyReadSectors,
            totalBlocksReadCount = totalBlocksRead
        )
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
