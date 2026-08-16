package com.nfcinspector.app.report

import com.nfcinspector.app.data.local.TechSerializer
import com.nfcinspector.app.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Enterprise-grade Report and Export Formatter for NFC Inspector.
 * Generates V2 Human-Readable Technical Reports and Versioned JSON Interoperability DTOs.
 *
 * Designed to be 100% decoupled from the UI layer to easily support future PDF and CSV exports.
 */
object ReportFormatter {

    /**
     * Formats timestamp into standard ISO 8601 string.
     */
    fun formatIso8601(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        return try {
            sdf.format(Date(timestamp))
        } catch (_: Exception) {
            val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            fallback.timeZone = TimeZone.getTimeZone("UTC")
            fallback.format(Date(timestamp))
        }
    }

    /**
     * Determines overall inspection status for reporting and metadata.
     */
    fun determineInspectionStatus(tag: TagRecord): String {
        val mfc = tag.mifareClassic
        if (mfc != null) {
            val map = mfc.memoryMap
            return when {
                map == null || !map.isInspected -> "structural"
                map.authenticatedSectorsCount == map.sectorCount && map.totalBlocksReadCount == map.blockCount -> "complete"
                map.authenticatedSectorsCount > 0 -> "partial"
                else -> "structural"
            }
        }
        if (tag.ndef != null) return "complete"
        return "structural"
    }

    /**
     * Generates a sanitized and filesystem-safe filename for export.
     */
    fun getExportFileName(tag: TagRecord, extension: String = "json"): String {
        val cleanUid = tag.uidContinuousHex.ifBlank { "TAG" }.replace(Regex("[^a-zA-Z0-9]"), "")
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val dateStr = sdf.format(Date(tag.timestamp))
        return "nfc-inspector_${cleanUid}_$dateStr.$extension"
    }

    // ==========================================
    // PARTE 2: RELATÓRIO TÉCNICO V2 (HUMAN READABLE)
    // ==========================================

    fun generateTechnicalReport(tag: TagRecord): String {
        val sb = StringBuilder()
        val inspectionStatus = when (determineInspectionStatus(tag)) {
            "complete" -> "Completa"
            "partial" -> "Parcial"
            else -> "Estrutural (Identificação Inicial)"
        }

        // Header
        sb.appendLine("==================================================")
        sb.appendLine("                  NFC INSPECTOR")
        sb.appendLine("          Relatório Técnico de Inspeção NFC")
        sb.appendLine("==================================================")
        sb.appendLine("ID da Leitura (UUID): ${tag.scanId}")
        sb.appendLine("Data e Hora:          ${tag.formattedDateTime}")
        sb.appendLine("Origem da Leitura:    ${tag.readerSource.displayName}")
        sb.appendLine("Status da Inspeção:   $inspectionStatus")
        sb.appendLine()

        // 1. RESUMO EXECUTIVO & IDENTIFICAÇÃO
        sb.appendLine("--- RESUMO & IDENTIFICAÇÃO ---")
        sb.appendLine("Tecnologia Principal: ${tag.mainTechnology}")
        sb.appendLine("UID (Hex Formatado):  ${tag.uidColonHex}")
        sb.appendLine("UID (Hex Contínuo):   ${tag.uidContinuousHex}")
        sb.appendLine("UID (Decimal):        ${tag.uidDecimal}")
        sb.appendLine("Tamanho do UID:       ${tag.uidLengthBytes} bytes (${tag.uidLengthBytes * 8} bits)")
        sb.appendLine()

        // 2. TECNOLOGIAS DETECTADAS
        sb.appendLine("--- TECNOLOGIAS DETECTADAS ---")
        if (tag.technologies.isEmpty()) {
            sb.appendLine("• Nenhuma tecnologia adicional detectada")
        } else {
            tag.technologies.forEach { tech ->
                sb.appendLine("• $tech")
            }
        }
        sb.appendLine()

        // 3. DETALHAMENTO DE CAMADA FÍSICA E PROTOCOLO
        tag.nfcA?.let {
            sb.appendLine("--- NFC-A (ISO 14443-3A) ---")
            sb.appendLine("ATQA (Answer To Request A): ${it.atqaHex}")
            sb.appendLine("SAK (Select Acknowledge):   ${it.sakHex}")
            sb.appendLine("Timeout de Resposta:        ${it.timeoutMs} ms")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.nfcB?.let {
            sb.appendLine("--- NFC-B (ISO 14443-3B) ---")
            sb.appendLine("Application Data:           ${it.appDataHex}")
            sb.appendLine("Protocol Info:              ${it.protocolInfoHex}")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.isoDep?.let {
            sb.appendLine("--- ISO-DEP (ISO 14443-4) ---")
            sb.appendLine("Historical Bytes:           ${it.historicalBytesHex ?: "N/A"}")
            sb.appendLine("HiLayer Response:           ${it.hiLayerResponseHex ?: "N/A"}")
            sb.appendLine("Suporte a APDU Estendido:   ${if (it.isExtendedLengthApduSupported) "Suportado" else "Não suportado"}")
            sb.appendLine("Timeout de Resposta:        ${it.timeoutMs} ms")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.mifareUltralight?.let {
            sb.appendLine("--- MIFARE ULTRALIGHT ---")
            sb.appendLine("Variante Detectada:         ${it.typeName}")
            sb.appendLine("Timeout de Resposta:        ${it.timeoutMs} ms")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.nfcF?.let {
            sb.appendLine("--- NFC-F (JIS 6319-4 / FeliCa) ---")
            sb.appendLine("System Code:                ${it.systemCodeHex ?: "N/A"}")
            sb.appendLine("Manufacturer Response:      ${it.manufacturerResponseHex ?: "N/A"}")
            sb.appendLine("Timeout de Resposta:        ${it.timeoutMs} ms")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.nfcV?.let {
            sb.appendLine("--- NFC-V (ISO 15693 / Vicinity) ---")
            sb.appendLine("DSFID:                      ${it.dsfidHex ?: "N/A"}")
            sb.appendLine("Response Flags:             ${it.responseFlagsHex ?: "N/A"}")
            sb.appendLine("Capacidade Máx. Transceive: ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        tag.ndef?.let {
            sb.appendLine("--- NDEF (NFC Data Exchange Format) ---")
            sb.appendLine("Tipo de Armazenamento:      ${it.typeName}")
            sb.appendLine("Status de Gravação:         ${if (it.isWritable) "Gravável (Leitura/Escrita)" else "Somente Leitura"}")
            sb.appendLine("Capacidade de Bloqueio:     ${if (it.canMakeReadOnly) "Permite bloqueio definitivo" else "Não bloqueável"}")
            sb.appendLine("Tamanho da Mensagem:        ${it.currentSizeBytes} bytes")
            sb.appendLine("Capacidade Total NDEF:      ${it.maxSizeBytes} bytes")
            sb.appendLine("Quantidade de Registros:    ${it.recordCount}")
            sb.appendLine()

            it.records.forEachIndexed { index, rec ->
                sb.appendLine("  [Registro NDEF #${index + 1}]")
                sb.appendLine("    TNF (Record Header):     ${rec.tnfName}")
                sb.appendLine("    Tipo do Registro:        ${rec.typeString}")
                if (rec.isText) {
                    sb.appendLine("    Idioma:                  ${rec.textLanguage ?: "N/A"}")
                    sb.appendLine("    Texto Decodificado:      ${rec.textContent}")
                } else if (rec.isUri) {
                    sb.appendLine("    URI Decodificada:        ${rec.uriContent}")
                } else if (rec.isMime) {
                    sb.appendLine("    Tipo MIME:               ${rec.mimeType}")
                }
                sb.appendLine("    Payload Bruto (HEX):     ${rec.rawPayloadHex}")
                sb.appendLine()
            }
        }

        if (tag.isNdefFormatable && tag.ndef == null) {
            sb.appendLine("--- NDEF FORMATABLE ---")
            sb.appendLine("A tag é compatível com formatação NDEF padrão, mas nenhuma mensagem foi gravada.")
            sb.appendLine()
        }

        // 4. MIFARE CLASSIC & MAPA DE MEMÓRIA
        tag.mifareClassic?.let { mfc ->
            sb.appendLine("--- MIFARE CLASSIC ---")
            sb.appendLine("Variante / Tipo:            ${mfc.typeName}")
            sb.appendLine("Capacidade Total:           ${MifareClassicMemoryMap.formatMifareCapacity(mfc.sizeBytes)}")
            sb.appendLine("Total de Setores:           ${mfc.sectorCount}")
            sb.appendLine("Total de Blocos:            ${mfc.blockCount}")
            sb.appendLine("Tamanho do Bloco:           ${mfc.blockSizeBytes} bytes")
            sb.appendLine()

            val map = mfc.memoryMap
            if (map != null) {
                sb.appendLine("--- RESULTADO DA INSPEÇÃO MIFARE CLASSIC ---")
                sb.appendLine("Setores Totais:             ${map.sectorCount}")
                sb.appendLine("Setores Autenticados:       ${map.authenticatedSectorsCount}")
                sb.appendLine("Setores Não Autenticados:   ${map.sectorCount - map.authenticatedSectorsCount}")
                sb.appendLine("Blocos Lidos:               ${map.totalBlocksReadCount} / ${map.blockCount}")
                val mfcStatusDesc = when {
                    !map.isInspected -> "Não inspecionado (somente identificação geométrica)"
                    map.authenticatedSectorsCount == map.sectorCount -> "Inspeção completa (100% dos setores autenticados)"
                    map.authenticatedSectorsCount > 0 -> "Inspeção parcial (${map.authenticatedSectorsCount}/${map.sectorCount} setores acessíveis)"
                    else -> "Nenhum setor autenticado com chaves padrão conhecidas"
                }
                sb.appendLine("Status da Leitura:          $mfcStatusDesc")
                sb.appendLine()

                sb.appendLine("--- MAPA DE MEMÓRIA DETALHADO ---")
                map.sectors.forEach { sector ->
                    val secNumStr = String.format(Locale.US, "%02d", sector.sectorIndex)
                    val isAuthSuccess = sector.status == MifareSectorStatus.READ_SUCCESS ||
                            sector.status == MifareSectorStatus.AUTH_KEY_A ||
                            sector.status == MifareSectorStatus.AUTH_KEY_B ||
                            sector.status == MifareSectorStatus.PARTIAL_READ

                    val authMethod = sector.authKeyType ?: "Nenhum"
                    val keyUsed = sector.authKeyName ?: "N/A"
                    val resultado = if (isAuthSuccess) "sucesso" else sector.status.label

                    sb.appendLine("Setor $secNumStr (${sector.blockCount} blocos, primeiro bloco: ${sector.firstBlockIndex})")
                    sb.appendLine("  Status:        ${sector.status.label}")
                    if (sector.authKeyType != null) {
                        sb.appendLine("  Autenticação:  $authMethod")
                        sb.appendLine("  Chave:         $keyUsed")
                        sb.appendLine("  Resultado:     $resultado")
                    }

                    sector.blocks.forEach { block ->
                        val blkNumStr = String.format(Locale.US, "%02d", block.blockIndex)
                        val typeLabel = block.blockType.label
                        sb.appendLine("  Bloco $blkNumStr — $typeLabel")
                        if (block.isReadSuccess) {
                            sb.appendLine("    HEX:   ${block.hexFormatted}")
                            sb.appendLine("    ASCII: ${block.asciiFormatted}")
                        } else {
                            sb.appendLine("    HEX:   [Não lido / Protegido]")
                        }
                    }

                    sector.accessBits?.let { ab ->
                        sb.appendLine("  [Bits de Acesso]")
                        sb.appendLine("    Bytes Brutos:  ${ab.rawBytesHex} | GPB: ${ab.gpbHex ?: "N/A"}")
                        if (ab.isValid) {
                            ab.trailerPermissions?.let { tp ->
                                sb.appendLine("    Sector Trailer: C1=${tp.c1}, C2=${tp.c2}, C3=${tp.c3}")
                                sb.appendLine("      Key A Write: ${tp.keyAWrite} | Access Bits R/W: ${tp.accessBitsRead}/${tp.accessBitsWrite} | Key B R/W: ${tp.keyBRead}/${tp.keyBWrite}")
                            }
                            ab.blockPermissions.forEach { dp ->
                                sb.appendLine("    ${dp.blockRangeLabel}: C1=${dp.c1}, C2=${dp.c2}, C3=${dp.c3}")
                                sb.appendLine("      Leitura: ${dp.readAccess} | Escrita: ${dp.writeAccess} | Inc: ${dp.incrementAccess} | Dec: ${dp.decrementTransferRestoreAccess}")
                            }
                        } else {
                            sb.appendLine("    Inconsistência: ${ab.inconsistencyError ?: "Bits de acesso corrompidos / invertidos"}")
                        }
                    }
                    sb.appendLine()
                }
            } else {
                sb.appendLine("Nenhum mapa de memória inspecionado. Para ler setores e blocos, execute a ação 'Inspecionar setores'.")
                sb.appendLine()
            }
        }

        // 5. OBSERVAÇÕES E NOTAS DE SEGURANÇA CONTEXTUAIS
        val notes = mutableListOf<String>()
        var noteCounter = 1

        if (tag.nfcA != null) {
            notes.add("${noteCounter++}. O UID é um identificador de camada de enlace (ISO 14443-3A). Em cartões regraváveis (Magic Cards), o UID pode ser clonado.")
        }
        if (tag.mifareClassic != null) {
            notes.add("${noteCounter++}. A autenticação MIFARE Classic baseia-se no algoritmo proprietário Crypto-1.")
        }
        notes.add("${noteCounter}. Privacidade: Operação 100% offline. Nenhum dado é transmitido a servidores.")

        sb.appendLine("==================================================")
        sb.appendLine("OBSERVAÇÕES E NOTAS:")
        notes.forEach { note ->
            sb.appendLine(note)
        }
        sb.appendLine("==================================================")
        sb.appendLine("Gerado por NFC Inspector")

        return sb.toString()
    }

    // ==========================================
    // PARTE 3: EXPORTAÇÃO ESTRUTURADA EM JSON
    // ==========================================

    /**
     * Generates a fully compliant, versioned, interoperable JSON export document (schemaVersion: 1).
     * Strictly sanitizes all cryptographic keys to prevent secret exposure.
     */
    fun generateJsonExport(tag: TagRecord): String {
        val root = JSONObject()

        // 1. Versioning & Generator Metadata
        root.put("schemaVersion", 1)
        root.put("scanId", tag.scanId)
        root.put("generator", JSONObject().apply {
            put("name", "NFC Inspector")
            put("platform", "Android")
        })
        root.put("reader", JSONObject().apply {
            put("source", tag.readerSource.sourceType.wireName)
            put("name", tag.readerSource.readerName)
            put("transport", tag.readerSource.transport)
            if (tag.readerSource.readerId != null) put("id", tag.readerSource.readerId)
        })
        root.put("capturedAt", formatIso8601(tag.timestamp))
        root.put("capturedAtTimestamp", tag.timestamp)
        root.put("inspectionStatus", determineInspectionStatus(tag))

        // 2. Tag Core Identifier
        root.put("tag", JSONObject().apply {
            put("uid", JSONObject().apply {
                put("hexColon", tag.uidColonHex)
                put("hex", tag.uidContinuousHex)
                put("decimal", tag.uidDecimal)
                put("lengthBytes", tag.uidLengthBytes)
                put("lengthBits", tag.uidLengthBytes * 8)
            })
            put("mainTechnology", tag.mainTechnology)
            val techArray = JSONArray()
            tag.technologies.forEach { techArray.put(it) }
            put("technologies", techArray)
            put("isNdefFormatable", tag.isNdefFormatable)
        })

        // 3. NFC-A (ISO 14443-3A)
        tag.nfcA?.let {
            root.put("nfcA", JSONObject().apply {
                put("atqa", it.atqaHex)
                put("sak", it.sakHex)
                put("timeoutMs", it.timeoutMs)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 4. NFC-B (ISO 14443-3B)
        tag.nfcB?.let {
            root.put("nfcB", JSONObject().apply {
                put("appData", it.appDataHex)
                put("protocolInfo", it.protocolInfoHex)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 5. ISO-DEP (ISO 14443-4)
        tag.isoDep?.let {
            root.put("isoDep", JSONObject().apply {
                if (it.historicalBytesHex != null) put("historicalBytes", it.historicalBytesHex)
                if (it.hiLayerResponseHex != null) put("hiLayerResponse", it.hiLayerResponseHex)
                put("isExtendedLengthApduSupported", it.isExtendedLengthApduSupported)
                put("timeoutMs", it.timeoutMs)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 6. MIFARE Ultralight
        tag.mifareUltralight?.let {
            root.put("mifareUltralight", JSONObject().apply {
                put("variant", it.typeName)
                put("timeoutMs", it.timeoutMs)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 7. NFC-F (FeliCa)
        tag.nfcF?.let {
            root.put("nfcF", JSONObject().apply {
                if (it.systemCodeHex != null) put("systemCode", it.systemCodeHex)
                if (it.manufacturerResponseHex != null) put("manufacturerResponse", it.manufacturerResponseHex)
                put("timeoutMs", it.timeoutMs)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 8. NFC-V (Vicinity)
        tag.nfcV?.let {
            root.put("nfcV", JSONObject().apply {
                if (it.dsfidHex != null) put("dsfid", it.dsfidHex)
                if (it.responseFlagsHex != null) put("responseFlags", it.responseFlagsHex)
                put("maxTransceiveBytes", it.maxTransceiveBytes)
            })
        }

        // 9. NDEF
        tag.ndef?.let { ndef ->
            root.put("ndef", JSONObject().apply {
                put("typeName", ndef.typeName)
                put("isWritable", ndef.isWritable)
                put("canMakeReadOnly", ndef.canMakeReadOnly)
                put("currentSizeBytes", ndef.currentSizeBytes)
                put("maxSizeBytes", ndef.maxSizeBytes)
                put("recordCount", ndef.recordCount)

                val recordsArray = JSONArray()
                ndef.records.forEach { rec ->
                    recordsArray.put(JSONObject().apply {
                        put("id", rec.id)
                        put("tnf", rec.tnfName)
                        put("type", rec.typeString)
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
                put("records", recordsArray)
            })
        }

        // 10. MIFARE Classic (Detailed Memory Map, Sectors, Blocks & Access Bits)
        tag.mifareClassic?.let { mfc ->
            val mfcObj = JSONObject().apply {
                put("variant", mfc.typeName)
                put("sizeBytes", mfc.sizeBytes)
                put("sectorCount", mfc.sectorCount)
                put("blockCount", mfc.blockCount)
                put("blockSizeBytes", mfc.blockSizeBytes)
                put("note", mfc.note)

                val map = mfc.memoryMap
                if (map != null) {
                    put("inspection", JSONObject().apply {
                        put("status", when {
                            map.authenticatedSectorsCount == map.sectorCount -> "complete"
                            map.authenticatedSectorsCount > 0 -> "partial"
                            else -> "structural"
                        })
                        put("authenticatedSectors", map.authenticatedSectorsCount)
                        put("unauthenticatedSectors", map.sectorCount - map.authenticatedSectorsCount)
                        put("fullyReadSectors", map.fullyReadSectorsCount)
                        put("blocksRead", map.totalBlocksReadCount)
                        put("totalBlocks", map.blockCount)
                    })

                    val sectorsArray = JSONArray()
                    map.sectors.forEach { sector ->
                        val secObj = JSONObject().apply {
                            put("sector", sector.sectorIndex)
                            put("blockCount", sector.blockCount)
                            put("firstBlock", sector.firstBlockIndex)
                            put("status", sector.status.name.lowercase(Locale.US))

                            // Authentication info (CRITICAL: NEVER output secret keys)
                            if (sector.authKeyType != null || sector.authKeyName != null) {
                                put("authentication", JSONObject().apply {
                                    put("method", sector.authKeyType?.lowercase(Locale.US)?.replace(" ", "_") ?: "unknown")
                                    put("keyName", sector.authKeyName ?: "Standard")
                                })
                            }

                            // Blocks inside sector
                            val blocksArray = JSONArray()
                            sector.blocks.forEach { block ->
                                val blkObj = JSONObject().apply {
                                    put("block", block.blockIndex)
                                    put("blockInSector", block.blockIndexInSector)
                                    put("type", block.blockType.name.lowercase(Locale.US))
                                    put("read", block.isReadSuccess)
                                    if (block.isReadSuccess) {
                                        if (block.rawBytes != null) {
                                            put("hex", TechSerializer.bytesToHex(block.rawBytes))
                                        } else {
                                            put("hex", block.hexFormatted.replace(" ", ""))
                                        }
                                        put("ascii", block.asciiFormatted)
                                    } else {
                                        if (block.readError != null) {
                                            put("error", block.readError)
                                        }
                                    }
                                }
                                blocksArray.put(blkObj)
                            }
                            put("blocks", blocksArray)

                            // Access Bits
                            sector.accessBits?.let { ab ->
                                val abObj = JSONObject().apply {
                                    put("valid", ab.isValid)
                                    put("raw", ab.rawBytesHex.replace(" ", ""))
                                    if (ab.gpbHex != null) put("gpb", ab.gpbHex)
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
                                            put("blockRange", bp.blockRangeLabel)
                                            put("group", bp.groupIndex)
                                            put("c1", bp.c1)
                                            put("c2", bp.c2)
                                            put("c3", bp.c3)
                                            put("read", bp.readAccess)
                                            put("write", bp.writeAccess)
                                            put("increment", bp.incrementAccess)
                                            put("decrementTransferRestore", bp.decrementTransferRestoreAccess)
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
            }
            root.put("mifareClassic", mfcObj)
        }

        return root.toString(2)
    }
}
