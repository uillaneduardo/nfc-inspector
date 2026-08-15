package com.nfcinspector.app.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.nfcinspector.app.data.model.NfcStatus
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.nfc.NfcManager
import com.nfcinspector.app.ui.theme.*
import com.nfcinspector.app.ui.viewmodel.MainViewModel

@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    onNavigateToReport: (TagRecord) -> Unit
) {
    val context = LocalContext.current
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    val currentTag by viewModel.currentTag.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Status Header Card
        when (val status = nfcStatus) {
            is NfcStatus.Unsupported -> {
                UnsupportedNfcCard()
            }
            is NfcStatus.Disabled -> {
                DisabledNfcCard(onEnableClick = {
                    NfcManager.openNfcSettings(context)
                })
            }
            is NfcStatus.ReadyWaiting -> {
                ReadyScanningCard()
            }
            is NfcStatus.TagDetected -> {
                // If a tag is currently shown
                ActiveDetectedBanner(
                    onScanAgain = { viewModel.resetToWaiting() }
                )
            }
            is NfcStatus.ScanError -> {
                ScanErrorCard(
                    errorMessage = status.message,
                    onRetry = { viewModel.resetToWaiting() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // If tag is detected, show full detailed inspection UI
        currentTag?.let { tag ->
            TagIdentificationCard(
                tag = tag,
                onCopy = { label, text ->
                    copyToClipboard(context, label, text)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TechListSection(tag = tag)

            Spacer(modifier = Modifier.height(12.dp))

            // Tech Detail Cards
            tag.nfcA?.let { nfcA ->
                NfcADetailCard(nfcA)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.isoDep?.let { isoDep ->
                IsoDepDetailCard(isoDep)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.ndef?.let { ndef ->
                NdefDetailCard(ndef)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.mifareClassic?.let { mfc ->
                MifareClassicDetailCard(mfc)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.mifareUltralight?.let { mfu ->
                MifareUltralightDetailCard(mfu)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.nfcB?.let { nfcB ->
                NfcBDetailCard(nfcB)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.nfcF?.let { nfcF ->
                NfcFDetailCard(nfcF)
                Spacer(modifier = Modifier.height(10.dp))
            }

            tag.nfcV?.let { nfcV ->
                NfcVDetailCard(nfcV)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action Buttons
            Button(
                onClick = { onNavigateToReport(tag) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
            ) {
                Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ver Relatório Técnico Completo", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun UnsupportedNfcCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Close, contentDescription = null, tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "✕ NFC não suportado",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ErrorRed)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NFC não disponível\nEste dispositivo não possui suporte a NFC ou o Android não disponibiliza um adaptador NFC compatível.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Você ainda pode consultar leituras salvas no Histórico e acessar as explicações técnicas na aba Sobre.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun DisabledNfcCard(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠ NFC desativado",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = WarningAmber)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NFC desativado\nPara identificar cartões e tags, é necessário ativar o NFC do aparelho.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onEnableClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ativar NFC", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ReadyScanningCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SignalGreen.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(SignalGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "✓ NFC ativo",
                    style = MaterialTheme.typography.labelMedium.copy(color = SignalGreen, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            Icon(
                Icons.Outlined.Nfc,
                contentDescription = "NFC Sensor",
                modifier = Modifier.size(56.dp),
                tint = TechBlue
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Aguardando cartão ou tag NFC...",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Aproxime a tag da traseira do aparelho para iniciar o diagnóstico técnico.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActiveDetectedBanner(onScanAgain: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SignalGreen.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cartão detectado com sucesso",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = SignalGreen)
                )
            }
            TextButton(onClick = onScanAgain) {
                Text("Ler Novo", color = TechBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ScanErrorCard(errorMessage: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ErrorRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Falha na leitura", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = ErrorRed))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(errorMessage, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Tentar Novamente")
            }
        }
    }
}

@Composable
fun TagIdentificationCard(
    tag: TagRecord,
    onCopy: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cartão detectado",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tag.formattedDateTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "TECNOLOGIA PRINCIPAL",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = tag.mainTechnology,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = TechBlue)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(14.dp))

            // UID Section
            Text(
                text = "UID (IDENTIFICADOR)",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag.uidColonHex,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                IconButton(onClick = { onCopy("UID", tag.uidColonHex) }) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "Copiar UID", tint = TechBlue)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hex contínuo:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tag.uidContinuousHex, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Decimal:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(tag.uidDecimal, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Comprimento:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${tag.uidLengthBytes} bytes (${tag.uidLengthBytes * 8} bits)", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ℹ " + tag.scanNotes,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TechListSection(tag: TagRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tecnologias Detectadas",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tag.technologies.forEach { tech ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "✓ $tech",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NfcADetailCard(nfcA: com.nfcinspector.app.data.model.NfcAParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ISO 14443-3A (NfcA)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("ATQA (Answer To Request A)", nfcA.atqaHex)
            TechRow("SAK (Select Acknowledge)", nfcA.sakHex)
            TechRow("Timeout", "${nfcA.timeoutMs} ms")
            TechRow("Tamanho Máx. Transceive", "${nfcA.maxTransceiveBytes} bytes")
        }
    }
}

@Composable
fun IsoDepDetailCard(isoDep: com.nfcinspector.app.data.model.IsoDepParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ISO 14443-4 (IsoDep)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Historical Bytes", isoDep.historicalBytesHex ?: "Não disponível neste cartão")
            TechRow("HiLayer Response", isoDep.hiLayerResponseHex ?: "Não disponível neste cartão")
            TechRow("Extended APDU", if (isoDep.isExtendedLengthApduSupported) "Suportado" else "Não suportado")
            TechRow("Timeout", "${isoDep.timeoutMs} ms")
            TechRow("Tamanho Máx. Transceive", "${isoDep.maxTransceiveBytes} bytes")
        }
    }
}

@Composable
fun NdefDetailCard(ndef: com.nfcinspector.app.data.model.NdefParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("NDEF (NFC Forum Data)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Tipo de Formato", ndef.typeName)
            TechRow("Gravável", if (ndef.isWritable) "Sim" else "Não")
            TechRow("Bloqueável para Leitura", if (ndef.canMakeReadOnly) "Sim" else "Não")
            TechRow("Tamanho Atual / Máximo", "${ndef.currentSizeBytes} / ${ndef.maxSizeBytes} bytes")
            TechRow("Quantidade de Registros", "${ndef.recordCount}")

            if (ndef.records.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Registros NDEF:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                ndef.records.forEachIndexed { idx, rec ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("Registro #${idx + 1} (${rec.tnfName})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            if (rec.isText) {
                                Text("Texto [${rec.textLanguage ?: "pt"}]: ${rec.textContent}", style = MaterialTheme.typography.bodyMedium)
                            } else if (rec.isUri) {
                                Text("URI: ${rec.uriContent}", style = MaterialTheme.typography.bodyMedium, color = TechBlue)
                            } else if (rec.isMime) {
                                Text("MIME (${rec.mimeType}): ${rec.rawPayloadHex}", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Payload: ${rec.rawPayloadHex}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MifareClassicDetailCard(mfc: com.nfcinspector.app.data.model.MifareClassicParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("MIFARE Classic", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Tipo Detectado", mfc.typeName)
            TechRow("Tamanho", "${mfc.sizeBytes} bytes")
            TechRow("Setores", "${mfc.sectorCount}")
            TechRow("Blocos", "${mfc.blockCount}")
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ℹ " + mfc.note,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MifareUltralightDetailCard(mfu: com.nfcinspector.app.data.model.MifareUltralightParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("MIFARE Ultralight / NTAG", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Tipo Detectado", mfu.typeName)
            TechRow("Max Transceive", "${mfu.maxTransceiveBytes} bytes")
            TechRow("Timeout", "${mfu.timeoutMs} ms")
        }
    }
}

@Composable
fun NfcBDetailCard(nfcB: com.nfcinspector.app.data.model.NfcBParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ISO 14443-3B (NfcB)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Application Data", nfcB.appDataHex)
            TechRow("Protocol Info", nfcB.protocolInfoHex)
            TechRow("Timeout", "${nfcB.timeoutMs} ms")
            TechRow("Max Transceive", "${nfcB.maxTransceiveBytes} bytes")
        }
    }
}

@Composable
fun NfcFDetailCard(nfcF: com.nfcinspector.app.data.model.NfcFParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("JIS 6319-4 (Sony FeliCa / NfcF)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("System Code", nfcF.systemCodeHex ?: "Não disponível neste cartão")
            TechRow("Manufacturer Response", nfcF.manufacturerResponseHex ?: "Não disponível neste cartão")
            TechRow("Timeout", "${nfcF.timeoutMs} ms")
            TechRow("Max Transceive", "${nfcF.maxTransceiveBytes} bytes")
        }
    }
}

@Composable
fun NfcVDetailCard(nfcV: com.nfcinspector.app.data.model.NfcVParams) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ISO 15693 (Vicinity / NfcV)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue))
            Spacer(modifier = Modifier.height(10.dp))
            TechRow("DSFID", nfcV.dsfidHex ?: "Não especificado")
            TechRow("Response Flags", nfcV.responseFlagsHex ?: "Não especificado")
            TechRow("Max Transceive", "${nfcV.maxTransceiveBytes} bytes")
        }
    }
}

@Composable
fun TechRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium))
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copiado para a área de transferência", Toast.LENGTH_SHORT).show()
}
