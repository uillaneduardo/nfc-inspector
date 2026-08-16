package com.nfcinspector.app.ui.screens.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcinspector.app.data.model.*
import com.nfcinspector.app.report.ReportFormatter
import com.nfcinspector.app.ui.theme.*
import java.util.Locale

/**
 * Structured Compose Visual Inspection View for ReportScreen.
 * Displays interactive cards for summary, protocols, memory map, sectors, blocks, and access bits.
 */
@Composable
fun ReportVisualView(
    tag: TagRecord,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Resumo Executivo & Identificação Geral
        ReportSummaryCard(tag = tag, onCopy = { label, value ->
            copyToClipboard(context, label, value)
        })

        // 2. Tecnologias Detectadas
        ReportTechChipsCard(technologies = tag.technologies, isNdefFormatable = tag.isNdefFormatable)

        // 3. NFC-A (se presente)
        tag.nfcA?.let { nfcA ->
            ReportNfcACard(nfcA = nfcA)
        }

        // 4. NFC-B (se presente)
        tag.nfcB?.let { nfcB ->
            ReportNfcBCard(nfcB = nfcB)
        }

        // 5. ISO-DEP (se presente)
        tag.isoDep?.let { isoDep ->
            ReportIsoDepCard(isoDep = isoDep)
        }

        // 6. MIFARE Ultralight (se presente)
        tag.mifareUltralight?.let { mfu ->
            ReportMifareUltralightCard(mfu = mfu)
        }

        // 7. NFC-F (se presente)
        tag.nfcF?.let { nfcF ->
            ReportNfcFCard(nfcF = nfcF)
        }

        // 8. NFC-V (se presente)
        tag.nfcV?.let { nfcV ->
            ReportNfcVCard(nfcV = nfcV)
        }

        // 9. NDEF (se presente)
        tag.ndef?.let { ndef ->
            ReportNdefCard(ndef = ndef)
        }

        // 10. MIFARE Classic & Mapa de Memória Completo
        tag.mifareClassic?.let { mfc ->
            ReportMifareClassicSection(mfc = mfc, onCopy = { label, value ->
                copyToClipboard(context, label, value)
            })
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "$label copiado!", Toast.LENGTH_SHORT).show()
}

// ---------------------------------------------------------------------------
// 1. Resumo Executivo Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportSummaryCard(
    tag: TagRecord,
    onCopy: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Identificação & Resumo",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tag.technologies.size} ${if (tag.technologies.size == 1) "tecnologia" else "tecnologias"}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // UID Principal em destaque
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "IDENTIFICADOR ÚNICO (UID)",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = TechBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tag.uidColonHex,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { onCopy("UID", tag.uidColonHex) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar UID", tint = TechBlue, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tabela com detalhes do UID e Tecnologia
            ReportMetricRow(label = "Tecnologia Principal", value = tag.mainTechnology)
            ReportMetricRow(label = "UID Hex Contínuo", value = tag.uidContinuousHex, isMonospace = true)
            ReportMetricRow(label = "UID Decimal", value = tag.uidDecimal, isMonospace = true)
            ReportMetricRow(label = "Tamanho do UID", value = "${tag.uidLengthBytes} bytes (${tag.uidLengthBytes * 8} bits)")

            tag.mifareClassic?.let { mfc ->
                ReportMetricRow(
                    label = "Capacidade MIFARE",
                    value = MifareClassicMemoryMap.formatMifareCapacity(mfc.sizeBytes)
                )
            }

            tag.ndef?.let { ndef ->
                ReportMetricRow(
                    label = "Capacidade NDEF",
                    value = "${ndef.currentSizeBytes} / ${ndef.maxSizeBytes} bytes"
                )
            }
        }
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// 2. Tecnologias Detectadas
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportTechChipsCard(
    technologies: List<String>,
    isNdefFormatable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Pilha de Tecnologias Detectadas",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                technologies.forEach { fullTech ->
                    val simpleName = fullTech.substringAfterLast(".")
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = simpleName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TechBlue
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = TechBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = TechBlue.copy(alpha = 0.08f)),
                        border = null
                    )
                }

                if (isNdefFormatable) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                text = "NdefFormatable",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SuccessGreen
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SuccessGreen.copy(alpha = 0.08f)),
                        border = null
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 3. NFC-A Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportNfcACard(nfcA: NfcAParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "NFC-A (ISO 14443-3A)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReportMetricRow(label = "ATQA (Answer To Request A)", value = nfcA.atqaHex, isMonospace = true)
            ReportMetricRow(label = "SAK (Select Acknowledge)", value = nfcA.sakHex, isMonospace = true)
            ReportMetricRow(label = "Timeout de Resposta", value = "${nfcA.timeoutMs} ms")
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${nfcA.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 4. NFC-B Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportNfcBCard(nfcB: NfcBParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "NFC-B (ISO 14443-3B)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReportMetricRow(label = "Application Data", value = nfcB.appDataHex, isMonospace = true)
            ReportMetricRow(label = "Protocol Info", value = nfcB.protocolInfoHex, isMonospace = true)
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${nfcB.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 5. ISO-DEP Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportIsoDepCard(isoDep: IsoDepParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Security, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "ISO-DEP (ISO 14443-4)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            isoDep.historicalBytesHex?.let {
                ReportMetricRow(label = "Historical Bytes (ATS)", value = it, isMonospace = true)
            }
            isoDep.hiLayerResponseHex?.let {
                ReportMetricRow(label = "HiLayer Response (ATTRIB)", value = it, isMonospace = true)
            }
            ReportMetricRow(
                label = "Suporte a APDU Estendido",
                value = if (isoDep.isExtendedLengthApduSupported) "Suportado" else "Não suportado"
            )
            ReportMetricRow(label = "Timeout de Resposta", value = "${isoDep.timeoutMs} ms")
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${isoDep.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 6. MIFARE Ultralight Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportMifareUltralightCard(mfu: MifareUltralightParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Memory, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "MIFARE Ultralight",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReportMetricRow(label = "Variante Detectada", value = mfu.typeName)
            ReportMetricRow(label = "Timeout de Resposta", value = "${mfu.timeoutMs} ms")
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${mfu.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 7. NFC-F (FeliCa) Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportNfcFCard(nfcF: NfcFParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "NFC-F (JIS 6319-4 / FeliCa)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            nfcF.systemCodeHex?.let {
                ReportMetricRow(label = "System Code", value = it, isMonospace = true)
            }
            nfcF.manufacturerResponseHex?.let {
                ReportMetricRow(label = "Manufacturer Response", value = it, isMonospace = true)
            }
            ReportMetricRow(label = "Timeout de Resposta", value = "${nfcF.timeoutMs} ms")
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${nfcF.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 8. NFC-V Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportNfcVCard(nfcV: NfcVParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Layers, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "NFC-V (ISO 15693 / Vicinity)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            nfcV.dsfidHex?.let {
                ReportMetricRow(label = "DSFID", value = it, isMonospace = true)
            }
            nfcV.responseFlagsHex?.let {
                ReportMetricRow(label = "Response Flags", value = it, isMonospace = true)
            }
            ReportMetricRow(label = "Capacidade Máxima Transceive", value = "${nfcV.maxTransceiveBytes} bytes")
        }
    }
}

// ---------------------------------------------------------------------------
// 9. NDEF Card
// ---------------------------------------------------------------------------
@Composable
private fun ReportNdefCard(ndef: NdefParams) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Description, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = "NDEF (NFC Data Exchange Format)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text(if (ndef.isWritable) "Gravável" else "Somente Leitura", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (ndef.isWritable) SuccessGreen.copy(alpha = 0.1f) else WarningAmber.copy(alpha = 0.1f)
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReportMetricRow(label = "Tipo de Armazenamento", value = ndef.typeName)
            ReportMetricRow(label = "Capacidade de Bloqueio", value = if (ndef.canMakeReadOnly) "Permite bloqueio" else "Não bloqueável")
            ReportMetricRow(label = "Tamanho Atual", value = "${ndef.currentSizeBytes} bytes")
            ReportMetricRow(label = "Capacidade Máxima", value = "${ndef.maxSizeBytes} bytes")
            ReportMetricRow(label = "Total de Registros", value = "${ndef.recordCount}")

            if (ndef.records.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Ocultar Registros NDEF" else "Ver ${ndef.records.size} Registros NDEF",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ndef.records.forEachIndexed { index, rec ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Registro #${index + 1} — ${rec.typeString}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Header (TNF): ${rec.tnfName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (rec.isText && rec.textContent != null) {
                                        Text(text = "Texto: ${rec.textContent}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    } else if (rec.isUri && rec.uriContent != null) {
                                        Text(text = "URI: ${rec.uriContent}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TechBlue)
                                    } else if (rec.isMime && rec.mimeType != null) {
                                        Text(text = "MIME: ${rec.mimeType}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Payload (Hex): ${rec.rawPayloadHex}",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// 10. MIFARE Classic & Mapa de Memória Estruturado
// ---------------------------------------------------------------------------
@Composable
private fun ReportMifareClassicSection(
    mfc: MifareClassicParams,
    onCopy: (String, String) -> Unit
) {
    val map = mfc.memoryMap

    // Card 1: Características MIFARE Classic
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Memory, contentDescription = null, tint = TechBlue, modifier = Modifier.size(18.dp))
                Text(
                    text = "MIFARE Classic",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ReportMetricRow(label = "Variante / Tipo", value = mfc.typeName)
            ReportMetricRow(label = "Capacidade Total", value = MifareClassicMemoryMap.formatMifareCapacity(mfc.sizeBytes))
            ReportMetricRow(label = "Total de Setores", value = "${mfc.sectorCount}")
            ReportMetricRow(label = "Total de Blocos", value = "${mfc.blockCount}")
            ReportMetricRow(label = "Tamanho do Bloco", value = "${mfc.blockSizeBytes} bytes")
        }
    }

    // Card 2: Resultado da Inspeção de Setores & Blocos
    if (map != null) {
        val authRatio = if (map.sectorCount > 0) map.authenticatedSectorsCount.toFloat() / map.sectorCount else 0f
        val blocksRatio = if (map.blockCount > 0) map.totalBlocksReadCount.toFloat() / map.blockCount else 0f

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Resultado da Inspeção de Memória",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Setores Autenticados
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Setores Autenticados", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${map.authenticatedSectorsCount} / ${map.sectorCount} setores (${(authRatio * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { authRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (authRatio == 1f) SuccessGreen else if (authRatio > 0f) WarningAmber else TechBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Blocos Lidos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Blocos Lidos com Sucesso", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${map.totalBlocksReadCount} / ${map.blockCount} blocos (${(blocksRatio * 100).toInt()}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { blocksRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (blocksRatio == 1f) SuccessGreen else if (blocksRatio > 0f) WarningAmber else TechBlue,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Card 3: Mapa de Memória Interativo e Expansível
        ReportMemoryMapSection(map = map, onCopy = onCopy)
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = TechBlue, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Mapa de Memória não inspecionado",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Para inspecionar setores e ler blocos de dados, utilize a ação 'Inspecionar setores' durante a leitura ativa.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Setores Expansíveis & Blocos
// ---------------------------------------------------------------------------
@Composable
private fun ReportMemoryMapSection(
    map: MifareClassicMemoryMap,
    onCopy: (String, String) -> Unit
) {
    var expandAll by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mapa de Memória Detalhado",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(
                    onClick = { expandAll = !expandAll },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (expandAll) "Recolher Todos" else "Expandir Todos",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            map.sectors.forEach { sector ->
                ReportSectorItem(
                    sector = sector,
                    forceExpanded = expandAll,
                    onCopy = onCopy
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ReportSectorItem(
    sector: MifareSectorData,
    forceExpanded: Boolean,
    onCopy: (String, String) -> Unit
) {
    var isExpanded by remember(forceExpanded) { mutableStateOf(forceExpanded) }
    var showAdvancedData by remember { mutableStateOf(false) }

    val isAuthSuccess = sector.status == MifareSectorStatus.READ_SUCCESS ||
            sector.status == MifareSectorStatus.AUTH_KEY_A ||
            sector.status == MifareSectorStatus.AUTH_KEY_B ||
            sector.status == MifareSectorStatus.PARTIAL_READ

    val statusColor = when {
        sector.status == MifareSectorStatus.READ_SUCCESS -> SuccessGreen
        isAuthSuccess -> TechBlue
        else -> WarningAmber
    }

    val secNumStr = String.format(Locale.US, "%02d", sector.sectorIndex)

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Sector Header Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, CircleShape)
                    )
                    Column {
                        Text(
                            text = "Setor $secNumStr",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        )
                        Text(
                            text = "${sector.blockCount} blocos (${sector.firstBlockIndex} a ${sector.firstBlockIndex + sector.blockCount - 1})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = { isExpanded = !isExpanded },
                        label = { Text(sector.status.label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = statusColor) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = statusColor.copy(alpha = 0.12f)),
                        border = null,
                        modifier = Modifier.height(26.dp)
                    )

                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Key details row
            if (sector.authKeyType != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Autenticação: ${sector.authKeyType} • ${sector.authKeyName ?: "Chave Padrão"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Sector Content: Blocks and Access Bits
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Blocks List
                    sector.blocks.forEach { block ->
                        ReportBlockRow(block = block, onCopy = onCopy)
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Access Bits Toggle
                    sector.accessBits?.let { ab ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedData = !showAdvancedData }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bits de Acesso (${ab.rawBytesHex} | GPB: ${ab.gpbHex ?: "N/A"})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue)
                            )
                            Icon(
                                if (showAdvancedData) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = TechBlue
                            )
                        }

                        AnimatedVisibility(visible = showAdvancedData) {
                            ReportAccessBitsDetail(accessBits = ab)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportBlockRow(
    block: MifareBlockData,
    onCopy: (String, String) -> Unit
) {
    val blkNumStr = String.format(Locale.US, "%02d", block.blockIndex)
    val typeLabel = when (block.blockType) {
        MifareBlockType.MANUFACTURER -> "Manufacturer"
        MifareBlockType.DATA -> "Data"
        MifareBlockType.SECTOR_TRAILER -> "Trailer"
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Bloco $blkNumStr",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = "• $typeLabel",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (block.isReadSuccess) {
                    IconButton(
                        onClick = { onCopy("Bloco $blkNumStr HEX", block.hexFormatted) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar Hex", tint = TechBlue, modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            if (block.isReadSuccess) {
                Text(
                    text = block.hexFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ASCII: ${block.asciiFormatted}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            } else {
                Text(
                    text = "[Não lido / Protegido]",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun ReportAccessBitsDetail(accessBits: MifareAccessBits) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (accessBits.isValid) {
                accessBits.trailerPermissions?.let { tp ->
                    Text(
                        text = "Sector Trailer: C1=${tp.c1} C2=${tp.c2} C3=${tp.c3}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    )
                    Text(
                        text = "Key A Write: ${tp.keyAWrite} | Access Bits R/W: ${tp.accessBitsRead}/${tp.accessBitsWrite} | Key B R/W: ${tp.keyBRead}/${tp.keyBWrite}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                accessBits.blockPermissions.forEach { bp ->
                    Text(
                        text = "${bp.blockRangeLabel}: C1=${bp.c1} C2=${bp.c2} C3=${bp.c3} — R: ${bp.readAccess} | W: ${bp.writeAccess}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "Aviso: ${accessBits.inconsistencyError ?: "Bits de acesso corrompidos ou inconsistentes"}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = WarningAmber)
                )
            }
        }
    }
}
