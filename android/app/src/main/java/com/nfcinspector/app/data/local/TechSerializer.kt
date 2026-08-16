package com.nfcinspector.app.data.local

import com.nfcinspector.app.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/**
 * Robust, zero-dependency serialization and deserialization utility for NFC technical models.
 * Ensures 100% offline persistence into SQLite/Room and reversible ByteArray operations.
 *
 * Security Guarantee: Secret hex keys (such as custom keys) are NEVER serialized.
 */
object TechSerializer {

    // --- Helper for ByteArray <-> Hex conversion ---

    fun bytesToHex(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789ABCDEF".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun hexToBytes(hex: String?): ByteArray? {
        if (hex.isNullOrBlank()) return null
        val clean = hex.replace(" ", "").replace(":", "").uppercase(Locale.US)
        if (clean.length % 2 != 0) return null
        val result = ByteArray(clean.length / 2)
        for (i in clean.indices step 2) {
            val high = Character.digit(clean[i], 16)
            val low = Character.digit(clean[i + 1], 16)
            if (high == -1 || low == -1) return null
            result[i / 2] = ((high shl 4) or low).toByte()
        }
        return result
    }

    // --- NFC-A ---

    fun serializeNfcA(params: NfcAParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            put("atqaHex", params.atqaHex)
            put("sakHex", params.sakHex)
            put("timeoutMs", params.timeoutMs)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
        }.toString()
    }

    fun deserializeNfcA(json: String?): NfcAParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            NfcAParams(
                atqaHex = obj.optString("atqaHex", ""),
                sakHex = obj.optString("sakHex", ""),
                timeoutMs = obj.optInt("timeoutMs", 0),
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- NFC-B ---

    fun serializeNfcB(params: NfcBParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            put("appDataHex", params.appDataHex)
            put("protocolInfoHex", params.protocolInfoHex)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
        }.toString()
    }

    fun deserializeNfcB(json: String?): NfcBParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            NfcBParams(
                appDataHex = obj.optString("appDataHex", ""),
                protocolInfoHex = obj.optString("protocolInfoHex", ""),
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- ISO-DEP ---

    fun serializeIsoDep(params: IsoDepParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            if (params.historicalBytesHex != null) put("historicalBytesHex", params.historicalBytesHex)
            if (params.hiLayerResponseHex != null) put("hiLayerResponseHex", params.hiLayerResponseHex)
            put("isExtendedLengthApduSupported", params.isExtendedLengthApduSupported)
            put("timeoutMs", params.timeoutMs)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
        }.toString()
    }

    fun deserializeIsoDep(json: String?): IsoDepParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            IsoDepParams(
                historicalBytesHex = if (obj.has("historicalBytesHex")) obj.optString("historicalBytesHex") else null,
                hiLayerResponseHex = if (obj.has("hiLayerResponseHex")) obj.optString("hiLayerResponseHex") else null,
                isExtendedLengthApduSupported = obj.optBoolean("isExtendedLengthApduSupported", false),
                timeoutMs = obj.optInt("timeoutMs", 0),
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- MIFARE Ultralight ---

    fun serializeMifareUltralight(params: MifareUltralightParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            put("typeName", params.typeName)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
            put("timeoutMs", params.timeoutMs)
        }.toString()
    }

    fun deserializeMifareUltralight(json: String?): MifareUltralightParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            MifareUltralightParams(
                typeName = obj.optString("typeName", "MIFARE Ultralight"),
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0),
                timeoutMs = obj.optInt("timeoutMs", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- NFC-F ---

    fun serializeNfcF(params: NfcFParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            if (params.systemCodeHex != null) put("systemCodeHex", params.systemCodeHex)
            if (params.manufacturerResponseHex != null) put("manufacturerResponseHex", params.manufacturerResponseHex)
            put("timeoutMs", params.timeoutMs)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
        }.toString()
    }

    fun deserializeNfcF(json: String?): NfcFParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            NfcFParams(
                systemCodeHex = if (obj.has("systemCodeHex")) obj.optString("systemCodeHex") else null,
                manufacturerResponseHex = if (obj.has("manufacturerResponseHex")) obj.optString("manufacturerResponseHex") else null,
                timeoutMs = obj.optInt("timeoutMs", 0),
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- NFC-V ---

    fun serializeNfcV(params: NfcVParams?): String? {
        if (params == null) return null
        return JSONObject().apply {
            if (params.dsfidHex != null) put("dsfidHex", params.dsfidHex)
            if (params.responseFlagsHex != null) put("responseFlagsHex", params.responseFlagsHex)
            put("maxTransceiveBytes", params.maxTransceiveBytes)
        }.toString()
    }

    fun deserializeNfcV(json: String?): NfcVParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            NfcVParams(
                dsfidHex = if (obj.has("dsfidHex")) obj.optString("dsfidHex") else null,
                responseFlagsHex = if (obj.has("responseFlagsHex")) obj.optString("responseFlagsHex") else null,
                maxTransceiveBytes = obj.optInt("maxTransceiveBytes", 0)
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- NDEF ---

    fun serializeNdef(params: NdefParams?): String? {
        if (params == null) return null
        val root = JSONObject().apply {
            put("isWritable", params.isWritable)
            put("canMakeReadOnly", params.canMakeReadOnly)
            put("typeName", params.typeName)
            put("currentSizeBytes", params.currentSizeBytes)
            put("maxSizeBytes", params.maxSizeBytes)
            put("recordCount", params.recordCount)

            val recArray = JSONArray()
            params.records.forEach { rec ->
                recArray.put(JSONObject().apply {
                    put("id", rec.id)
                    put("tnfName", rec.tnfName)
                    put("typeString", rec.typeString)
                    put("isText", rec.isText)
                    put("isUri", rec.isUri)
                    put("isMime", rec.isMime)
                    put("isExternal", rec.isExternal)
                    if (rec.textLanguage != null) put("textLanguage", rec.textLanguage)
                    if (rec.textContent != null) put("textContent", rec.textContent)
                    if (rec.uriContent != null) put("uriContent", rec.uriContent)
                    if (rec.mimeType != null) put("mimeType", rec.mimeType)
                    put("rawPayloadHex", rec.rawPayloadHex)
                })
            }
            put("records", recArray)
        }
        return root.toString()
    }

    fun deserializeNdef(json: String?): NdefParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            val recArray = obj.optJSONArray("records") ?: JSONArray()
            val recordsList = mutableListOf<NdefRecordItem>()
            for (i in 0 until recArray.length()) {
                val rObj = recArray.getJSONObject(i)
                recordsList.add(
                    NdefRecordItem(
                        id = rObj.optString("id", ""),
                        tnfName = rObj.optString("tnfName", ""),
                        typeString = rObj.optString("typeString", ""),
                        isText = rObj.optBoolean("isText", false),
                        isUri = rObj.optBoolean("isUri", false),
                        isMime = rObj.optBoolean("isMime", false),
                        isExternal = rObj.optBoolean("isExternal", false),
                        textLanguage = if (rObj.has("textLanguage")) rObj.optString("textLanguage") else null,
                        textContent = if (rObj.has("textContent")) rObj.optString("textContent") else null,
                        uriContent = if (rObj.has("uriContent")) rObj.optString("uriContent") else null,
                        mimeType = if (rObj.has("mimeType")) rObj.optString("mimeType") else null,
                        rawPayloadHex = rObj.optString("rawPayloadHex", "")
                    )
                )
            }
            NdefParams(
                isWritable = obj.optBoolean("isWritable", false),
                canMakeReadOnly = obj.optBoolean("canMakeReadOnly", false),
                typeName = obj.optString("typeName", "NDEF"),
                currentSizeBytes = obj.optInt("currentSizeBytes", 0),
                maxSizeBytes = obj.optInt("maxSizeBytes", 0),
                recordCount = obj.optInt("recordCount", recordsList.size),
                records = recordsList
            )
        } catch (_: Exception) {
            null
        }
    }

    // --- MIFARE Classic (Full Memory Map & Security Sanitization) ---

    fun serializeMifareClassic(params: MifareClassicParams?): String? {
        if (params == null) return null
        val root = JSONObject().apply {
            put("typeName", params.typeName)
            put("sizeBytes", params.sizeBytes)
            put("sectorCount", params.sectorCount)
            put("blockCount", params.blockCount)
            put("blockSizeBytes", params.blockSizeBytes)
            put("note", params.note)

            params.memoryMap?.let { map ->
                val mapObj = JSONObject().apply {
                    put("typeName", map.typeName)
                    put("sizeBytes", map.sizeBytes)
                    put("sectorCount", map.sectorCount)
                    put("blockCount", map.blockCount)
                    put("blockSizeBytes", map.blockSizeBytes)
                    put("isInspected", map.isInspected)
                    put("authenticatedSectorsCount", map.authenticatedSectorsCount)
                    put("fullyReadSectorsCount", map.fullyReadSectorsCount)
                    put("totalBlocksReadCount", map.totalBlocksReadCount)

                    val sectorsArray = JSONArray()
                    map.sectors.forEach { sector ->
                        val secObj = JSONObject().apply {
                            put("sectorIndex", sector.sectorIndex)
                            put("blockCount", sector.blockCount)
                            put("firstBlockIndex", sector.firstBlockIndex)
                            put("status", sector.status.name)
                            if (sector.authKeyType != null) put("authKeyType", sector.authKeyType)
                            if (sector.authKeyName != null) put("authKeyName", sector.authKeyName)
                            // CRITICAL SECURITY: Never serialize authKeyUsedHex to database/export

                            val blocksArray = JSONArray()
                            sector.blocks.forEach { block ->
                                val blkObj = JSONObject().apply {
                                    put("blockIndex", block.blockIndex)
                                    put("blockIndexInSector", block.blockIndexInSector)
                                    put("blockType", block.blockType.name)
                                    put("isReadSuccess", block.isReadSuccess)
                                    put("hexFormatted", block.hexFormatted)
                                    put("asciiFormatted", block.asciiFormatted)
                                    if (block.readError != null) put("readError", block.readError)
                                    if (block.rawBytes != null) {
                                        put("rawBytesHex", bytesToHex(block.rawBytes))
                                    }
                                }
                                blocksArray.put(blkObj)
                            }
                            put("blocks", blocksArray)

                            sector.accessBits?.let { ab ->
                                val abObj = JSONObject().apply {
                                    put("rawBytesHex", ab.rawBytesHex)
                                    if (ab.gpbHex != null) put("gpbHex", ab.gpbHex)
                                    put("isValid", ab.isValid)
                                    if (ab.inconsistencyError != null) put("inconsistencyError", ab.inconsistencyError)

                                    ab.trailerPermissions?.let { tp ->
                                        put("trailerPermissions", JSONObject().apply {
                                            put("c1", tp.c1)
                                            put("c2", tp.c2)
                                            put("c3", tp.c3)
                                            put("keyARead", tp.keyARead)
                                            put("keyAWrite", tp.keyAWrite)
                                            put("accessBitsRead", tp.accessBitsRead)
                                            put("accessBitsWrite", tp.accessBitsWrite)
                                            put("keyBRead", tp.keyBRead)
                                            put("keyBWrite", tp.keyBWrite)
                                        })
                                    }

                                    val blockPermsArray = JSONArray()
                                    ab.blockPermissions.forEach { bp ->
                                        blockPermsArray.put(JSONObject().apply {
                                            put("blockRangeLabel", bp.blockRangeLabel)
                                            put("groupIndex", bp.groupIndex)
                                            put("c1", bp.c1)
                                            put("c2", bp.c2)
                                            put("c3", bp.c3)
                                            put("readAccess", bp.readAccess)
                                            put("writeAccess", bp.writeAccess)
                                            put("incrementAccess", bp.incrementAccess)
                                            put("decrementTransferRestoreAccess", bp.decrementTransferRestoreAccess)
                                        })
                                    }
                                    put("blockPermissions", blockPermsArray)
                                }
                                put("accessBits", abObj)
                            }
                        }
                        sectorsArray.put(secObj)
                    }
                    put("sectors", sectorsArray)
                }
                put("memoryMap", mapObj)
            }
        }
        return root.toString()
    }

    fun deserializeMifareClassic(json: String?): MifareClassicParams? {
        if (json.isNullOrBlank()) return null
        return try {
            val obj = JSONObject(json)
            val typeName = obj.optString("typeName", "MIFARE Classic")
            val sizeBytes = obj.optInt("sizeBytes", 1024)
            val sectorCount = obj.optInt("sectorCount", 16)
            val blockCount = obj.optInt("blockCount", 64)
            val blockSizeBytes = obj.optInt("blockSizeBytes", 16)
            val note = obj.optString("note", "")

            val memoryMapObj = obj.optJSONObject("memoryMap")
            val memoryMap = if (memoryMapObj != null) {
                val mapSectors = mutableListOf<MifareSectorData>()
                val secArray = memoryMapObj.optJSONArray("sectors") ?: JSONArray()

                for (s in 0 until secArray.length()) {
                    val sObj = secArray.getJSONObject(s)
                    val sectorIndex = sObj.optInt("sectorIndex", s)
                    val sBlockCount = sObj.optInt("blockCount", 4)
                    val firstBlockIndex = sObj.optInt("firstBlockIndex", sectorIndex * 4)
                    val statusStr = sObj.optString("status", MifareSectorStatus.NOT_TESTED.name)
                    val status = try {
                        MifareSectorStatus.valueOf(statusStr)
                    } catch (_: Exception) {
                        MifareSectorStatus.NOT_TESTED
                    }
                    val authKeyType = if (sObj.has("authKeyType")) sObj.optString("authKeyType") else null
                    val authKeyName = if (sObj.has("authKeyName")) sObj.optString("authKeyName") else null

                    // Blocks
                    val blocksList = mutableListOf<MifareBlockData>()
                    val blkArray = sObj.optJSONArray("blocks") ?: JSONArray()
                    for (b in 0 until blkArray.length()) {
                        val bObj = blkArray.getJSONObject(b)
                        val blockIndex = bObj.optInt("blockIndex", firstBlockIndex + b)
                        val blockIndexInSector = bObj.optInt("blockIndexInSector", b)
                        val bTypeStr = bObj.optString("blockType", MifareBlockType.DATA.name)
                        val blockType = try {
                            MifareBlockType.valueOf(bTypeStr)
                        } catch (_: Exception) {
                            MifareBlockType.DATA
                        }
                        val isReadSuccess = bObj.optBoolean("isReadSuccess", false)
                        val hexFormatted = bObj.optString("hexFormatted", "Não lido / Protegido")
                        val asciiFormatted = bObj.optString("asciiFormatted", "—")
                        val readError = if (bObj.has("readError")) bObj.optString("readError") else null
                        val rawBytesHex = if (bObj.has("rawBytesHex")) bObj.optString("rawBytesHex") else null
                        val rawBytes = hexToBytes(rawBytesHex)

                        blocksList.add(
                            MifareBlockData(
                                blockIndex = blockIndex,
                                blockIndexInSector = blockIndexInSector,
                                blockType = blockType,
                                rawBytes = rawBytes,
                                hexFormatted = hexFormatted,
                                asciiFormatted = asciiFormatted,
                                isReadSuccess = isReadSuccess,
                                readError = readError
                            )
                        )
                    }

                    // AccessBits
                    val abObj = sObj.optJSONObject("accessBits")
                    val accessBits = if (abObj != null) {
                        val rawBytesHex = abObj.optString("rawBytesHex", "")
                        val gpbHex = if (abObj.has("gpbHex")) abObj.optString("gpbHex") else null
                        val isValid = abObj.optBoolean("isValid", false)
                        val inconsistencyError = if (abObj.has("inconsistencyError")) abObj.optString("inconsistencyError") else null

                        val tpObj = abObj.optJSONObject("trailerPermissions")
                        val trailerPermissions = if (tpObj != null) {
                            TrailerAccessPermissions(
                                c1 = tpObj.optInt("c1", 0),
                                c2 = tpObj.optInt("c2", 0),
                                c3 = tpObj.optInt("c3", 1),
                                keyARead = tpObj.optString("keyARead", "Nunca (Protegido por HW)"),
                                keyAWrite = tpObj.optString("keyAWrite", "Key A"),
                                accessBitsRead = tpObj.optString("accessBitsRead", "Key A"),
                                accessBitsWrite = tpObj.optString("accessBitsWrite", "Nunca"),
                                keyBRead = tpObj.optString("keyBRead", "Key A"),
                                keyBWrite = tpObj.optString("keyBWrite", "Key A")
                            )
                        } else null

                        val bpArray = abObj.optJSONArray("blockPermissions") ?: JSONArray()
                        val blockPermsList = mutableListOf<BlockAccessPermissions>()
                        for (bpIdx in 0 until bpArray.length()) {
                            val bpItem = bpArray.getJSONObject(bpIdx)
                            blockPermsList.add(
                                BlockAccessPermissions(
                                    blockRangeLabel = bpItem.optString("blockRangeLabel", ""),
                                    groupIndex = bpItem.optInt("groupIndex", bpIdx),
                                    c1 = bpItem.optInt("c1", 0),
                                    c2 = bpItem.optInt("c2", 0),
                                    c3 = bpItem.optInt("c3", 0),
                                    readAccess = bpItem.optString("readAccess", "Key A|B"),
                                    writeAccess = bpItem.optString("writeAccess", "Key A|B"),
                                    incrementAccess = bpItem.optString("incrementAccess", "Key A|B"),
                                    decrementTransferRestoreAccess = bpItem.optString("decrementTransferRestoreAccess", "Key A|B")
                                )
                            )
                        }

                        MifareAccessBits(
                            rawBytesHex = rawBytesHex,
                            gpbHex = gpbHex,
                            isValid = isValid,
                            inconsistencyError = inconsistencyError,
                            blockPermissions = blockPermsList,
                            trailerPermissions = trailerPermissions
                        )
                    } else null

                    mapSectors.add(
                        MifareSectorData(
                            sectorIndex = sectorIndex,
                            blockCount = sBlockCount,
                            firstBlockIndex = firstBlockIndex,
                            status = status,
                            authKeyType = authKeyType,
                            authKeyName = authKeyName,
                            authKeyUsedHex = null, // Sanitized
                            blocks = blocksList,
                            accessBits = accessBits
                        )
                    )
                }

                MifareClassicMemoryMap(
                    typeName = memoryMapObj.optString("typeName", typeName),
                    sizeBytes = memoryMapObj.optInt("sizeBytes", sizeBytes),
                    sectorCount = memoryMapObj.optInt("sectorCount", sectorCount),
                    blockCount = memoryMapObj.optInt("blockCount", blockCount),
                    blockSizeBytes = memoryMapObj.optInt("blockSizeBytes", blockSizeBytes),
                    sectors = mapSectors,
                    isInspected = memoryMapObj.optBoolean("isInspected", true),
                    authenticatedSectorsCount = memoryMapObj.optInt("authenticatedSectorsCount", 0),
                    fullyReadSectorsCount = memoryMapObj.optInt("fullyReadSectorsCount", 0),
                    totalBlocksReadCount = memoryMapObj.optInt("totalBlocksReadCount", 0)
                )
            } else null

            MifareClassicParams(
                typeName = typeName,
                sizeBytes = sizeBytes,
                sectorCount = sectorCount,
                blockCount = blockCount,
                blockSizeBytes = blockSizeBytes,
                memoryMap = memoryMap,
                note = note
            )
        } catch (_: Exception) {
            null
        }
    }
}
