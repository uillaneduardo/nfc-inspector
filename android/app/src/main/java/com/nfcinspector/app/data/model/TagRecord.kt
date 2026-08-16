package com.nfcinspector.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TagRecord(
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val uidColonHex: String,
    val uidContinuousHex: String,
    val uidDecimal: String,
    val uidLengthBytes: Int,
    val mainTechnology: String,
    val technologies: List<String>,
    val nfcA: NfcAParams? = null,
    val nfcB: NfcBParams? = null,
    val isoDep: IsoDepParams? = null,
    val mifareClassic: MifareClassicParams? = null,
    val mifareUltralight: MifareUltralightParams? = null,
    val nfcF: NfcFParams? = null,
    val nfcV: NfcVParams? = null,
    val ndef: NdefParams? = null,
    val isNdefFormatable: Boolean = false,
    val scanNotes: String = "UID é um identificador de camada física e não deve ser considerado automaticamente uma credencial segura."
) {
    val formattedDateTime: String
        get() {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun generateFullReport(): String {
        val sb = StringBuilder()
        sb.appendLine("===============================")
        sb.appendLine("        NFC INSPECTOR")
        sb.appendLine("    Relatório Técnico Detalhado")
        sb.appendLine("===============================")
        sb.appendLine("Data e Hora: $formattedDateTime")
        sb.appendLine("Tecnologia Principal: $mainTechnology")
        sb.appendLine()
        sb.appendLine("--- IDENTIFICADOR (UID) ---")
        sb.appendLine("Hex com separadores: $uidColonHex")
        sb.appendLine("Hex contínuo:        $uidContinuousHex")
        sb.appendLine("Decimal:             $uidDecimal")
        sb.appendLine("Tamanho:             $uidLengthBytes bytes (${uidLengthBytes * 8} bits)")
        sb.appendLine()
        sb.appendLine("--- TECNOLOGIAS DETECTADAS ---")
        technologies.forEach { tech ->
            sb.appendLine("• $tech")
        }
        sb.appendLine()

        nfcA?.let {
            sb.appendLine("--- NFC-A (ISO 14443-3A) ---")
            sb.appendLine("ATQA:                ${it.atqaHex}")
            sb.appendLine("SAK:                 ${it.sakHex}")
            sb.appendLine("Timeout:             ${it.timeoutMs} ms")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        nfcB?.let {
            sb.appendLine("--- NFC-B (ISO 14443-3B) ---")
            sb.appendLine("Application Data:    ${it.appDataHex}")
            sb.appendLine("Protocol Info:       ${it.protocolInfoHex}")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        isoDep?.let {
            sb.appendLine("--- ISO-DEP (ISO 14443-4) ---")
            sb.appendLine("Historical Bytes:    ${it.historicalBytesHex ?: "N/A"}")
            sb.appendLine("HiLayer Response:    ${it.hiLayerResponseHex ?: "N/A"}")
            sb.appendLine("Extended APDU:       ${if (it.isExtendedLengthApduSupported) "Suportado" else "Não suportado"}")
            sb.appendLine("Timeout:             ${it.timeoutMs} ms")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        mifareClassic?.let {
            sb.appendLine("--- MIFARE CLASSIC ---")
            sb.appendLine("Tipo:                ${it.typeName}")
            sb.appendLine("Capacidade:          ${MifareClassicMemoryMap.formatMifareCapacity(it.sizeBytes)}")
            sb.appendLine("Setores:             ${it.sectorCount}")
            sb.appendLine("Blocos Totais:       ${it.blockCount}")
            sb.appendLine("Tamanho do Bloco:    ${it.blockSizeBytes} bytes")
            sb.appendLine("Nota:                ${it.note}")
            sb.appendLine()

            it.memoryMap?.let { map ->
                sb.appendLine("--- MAPA DE MEMÓRIA MIFARE CLASSIC ---")
                sb.appendLine("Setores Autenticados: ${map.authenticatedSectorsCount} / ${map.sectorCount}")
                sb.appendLine("Blocos Lidos:         ${map.totalBlocksReadCount} / ${map.blockCount}")
                sb.appendLine()

                map.sectors.forEach { sector ->
                    val secNumStr = String.format(Locale.US, "%02d", sector.sectorIndex)
                    sb.appendLine("Setor $secNumStr (${sector.blockCount} blocos)")
                    sb.appendLine("  Status:        ${sector.status.label}")
                    if (sector.authKeyType != null) {
                        sb.appendLine("  Autenticação:  ${sector.authKeyType}")
                        if (sector.authKeyName != null) {
                            sb.appendLine("  Chave:         ${sector.authKeyName}")
                        }
                        val resultado = if (sector.status == MifareSectorStatus.READ_SUCCESS || sector.status == MifareSectorStatus.AUTH_KEY_A || sector.status == MifareSectorStatus.AUTH_KEY_B || sector.status == MifareSectorStatus.PARTIAL_READ || sector.status == MifareSectorStatus.AUTHENTICATED_READ) "sucesso" else sector.status.label
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
                                sb.appendLine("      Key A Write: ${tp.keyAWrite} | Access Bits Read/Write: ${tp.accessBitsRead}/${tp.accessBitsWrite} | Key B Read/Write: ${tp.keyBRead}/${tp.keyBWrite}")
                            }
                            ab.blockPermissions.forEach { dp ->
                                sb.appendLine("    ${dp.blockRangeLabel}: C1=${dp.c1}, C2=${dp.c2}, C3=${dp.c3}")
                                sb.appendLine("      Leitura: ${dp.readAccess} | Escrita: ${dp.writeAccess} | Inc: ${dp.incrementAccess} | Dec: ${dp.decrementTransferRestoreAccess}")
                            }
                        } else {
                            sb.appendLine("    Inconsistência: ${ab.inconsistencyError ?: "Inconsistente"}")
                        }
                    }
                    sb.appendLine()
                }
            }
        }

        mifareUltralight?.let {
            sb.appendLine("--- MIFARE ULTRALIGHT ---")
            sb.appendLine("Tipo:                ${it.typeName}")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine("Timeout:             ${it.timeoutMs} ms")
            sb.appendLine()
        }

        nfcF?.let {
            sb.appendLine("--- NFC-F (JIS 6319-4 / FeliCa) ---")
            sb.appendLine("System Code:         ${it.systemCodeHex ?: "N/A"}")
            sb.appendLine("Mfr Response:        ${it.manufacturerResponseHex ?: "N/A"}")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        nfcV?.let {
            sb.appendLine("--- NFC-V (ISO 15693 / Vicinity) ---")
            sb.appendLine("DSFID:               ${it.dsfidHex ?: "N/A"}")
            sb.appendLine("Response Flags:      ${it.responseFlagsHex ?: "N/A"}")
            sb.appendLine("Max Transceive:      ${it.maxTransceiveBytes} bytes")
            sb.appendLine()
        }

        ndef?.let {
            sb.appendLine("--- NDEF (NFC Data Exchange Format) ---")
            sb.appendLine("Tipo de Armazenamento: ${it.typeName}")
            sb.appendLine("Gravável:              ${if (it.isWritable) "Sim" else "Não"}")
            sb.appendLine("Bloqueável p/ Leitura: ${if (it.canMakeReadOnly) "Sim" else "Não"}")
            sb.appendLine("Tamanho Atual:         ${it.currentSizeBytes} bytes")
            sb.appendLine("Capacidade Máxima:     ${it.maxSizeBytes} bytes")
            sb.appendLine("Total de Registros:    ${it.recordCount}")
            it.records.forEachIndexed { index, rec ->
                sb.appendLine("  [Registro #${index + 1}]")
                sb.appendLine("    TNF:             ${rec.tnfName}")
                sb.appendLine("    Tipo:            ${rec.typeString}")
                if (rec.isText) {
                    sb.appendLine("    Idioma:          ${rec.textLanguage ?: "N/A"}")
                    sb.appendLine("    Texto:           ${rec.textContent}")
                } else if (rec.isUri) {
                    sb.appendLine("    URI:             ${rec.uriContent}")
                } else if (rec.isMime) {
                    sb.appendLine("    MIME Type:       ${rec.mimeType}")
                }
                sb.appendLine("    Payload (Hex):   ${rec.rawPayloadHex}")
            }
            sb.appendLine()
        }

        if (isNdefFormatable && ndef == null) {
            sb.appendLine("--- NDEF FORMATABLE ---")
            sb.appendLine("Tag compatível com formatação NDEF. Nenhuma estrutura NDEF válida foi detectada.")
            sb.appendLine()
        }

        sb.appendLine("===============================")
        sb.appendLine("Privacidade: 100% Offline e Seguro.")
        sb.appendLine("Gerado por NFC Inspector.")
        return sb.toString()
    }
}
