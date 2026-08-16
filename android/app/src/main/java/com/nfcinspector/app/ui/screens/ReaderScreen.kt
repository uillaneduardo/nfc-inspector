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
import java.util.Locale

@Composable
fun ReaderScreen(
    viewModel: MainViewModel,
    onNavigateToReport: (TagRecord) -> Unit
) {
    val context = LocalContext.current
    val nfcStatus by viewModel.nfcStatus.collectAsState()
    val currentTag by viewModel.currentTag.collectAsState()
    val isMifareInspecting by viewModel.isMifareInspecting.collectAsState()
    val mifareInspectionMessage by viewModel.mifareInspectionStatusMessage.collectAsState()
    val isSaved by viewModel.isCurrentTagSaved.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
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
            is NfcStatus.Checking -> {
                CheckingNfcCard()
            }
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
                MifareClassicDetailCard(
                    mfc = mfc,
                    isInspecting = isMifareInspecting,
                    inspectionMessage = mifareInspectionMessage,
                    onInspect = { viewModel.inspectMifareSectors() }
                )
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

            // Action Buttons (Save manually + View Report)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.saveCurrentScanManually { alreadySaved ->
                            if (alreadySaved) {
                                Toast.makeText(context, "Esta leitura já está salva no histórico.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "✓ Leitura salva com sucesso no histórico!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isSaved && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isSaved) {
                        ButtonDefaults.outlinedButtonColors(
                            containerColor = SignalGreen.copy(alpha = 0.12f),
                            contentColor = SignalGreen
                        )
                    } else {
                        ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else if (isSaved) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvo", fontWeight = FontWeight.SemiBold, color = SignalGreen)
                    } else {
                        Icon(Icons.Outlined.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salvar leitura", fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = { onNavigateToReport(tag) },
                    modifier = Modifier
                        .weight(1.3f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                ) {
                    Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Relatório", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CheckingNfcCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = TechBlue
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Verificando NFC...",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
fun MifareClassicDetailCard(
    mfc: com.nfcinspector.app.data.model.MifareClassicParams,
    isInspecting: Boolean,
    inspectionMessage: String?,
    onInspect: () -> Unit
) {
    val context = LocalContext.current
    var isExpandedMap by remember { mutableStateOf(true) }
    var selectedSectorFilter by remember { mutableStateOf(0) } // 0: Todos, 1: Autenticados, 2: Falhas

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = TechBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mfc.typeName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TechBlue)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TechBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${com.nfcinspector.app.data.model.MifareClassicMemoryMap.formatMifareCapacityShort(mfc.sizeBytes)} (${mfc.sectorCount} Setores)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TechBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            TechRow("Variante / Tipo", mfc.typeName)
            TechRow("Capacidade Total", com.nfcinspector.app.data.model.MifareClassicMemoryMap.formatMifareCapacity(mfc.sizeBytes))
            TechRow("Quantidade de Setores", "${mfc.sectorCount}")
            TechRow("Total de Blocos", "${mfc.blockCount}")
            TechRow("Tamanho do Bloco", "${mfc.blockSizeBytes} bytes")

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(8.dp)
            ) {
                Text(
                    text = "ℹ " + mfc.note,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Explicit Inspection Action Banner & Controls
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            val memoryMap = mfc.memoryMap
            val hasBeenInspected = memoryMap?.isInspected == true

            if (isInspecting) {
                // Processing State
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = TechBlue.copy(alpha = 0.10f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = TechBlue
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Inspecionando setores...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TechBlue)
                            )
                            Text(
                                text = "Mantenha a tag próxima ao aparelho durante a leitura.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                if (!hasBeenInspected) {
                    // Initial State: Prompt user to inspect sectors
                    Button(
                        onClick = onInspect,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inspecionar setores",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Mantenha a tag próxima ao aparelho para testar autenticação e ler os blocos com chaves padrão conhecidas.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                } else {
                    // Already inspected: Show repeat/retry button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Inspeção Realizada",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = SignalGreen)
                            )
                            Text(
                                text = "${memoryMap.authenticatedSectorsCount}/${memoryMap.sectorCount} setores autenticados",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onInspect,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reinspecionar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Inspection Feedback / Error / Status Message
            if (inspectionMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = inspectionMessage,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Memory map inspection section
            if (memoryMap != null) {
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mapa Estrutural da Memória",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${memoryMap.authenticatedSectorsCount}/${memoryMap.sectorCount} setores autenticados • ${memoryMap.totalBlocksReadCount}/${memoryMap.blockCount} blocos lidos",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { isExpandedMap = !isExpandedMap }) {
                        Icon(
                            imageVector = if (isExpandedMap) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpandedMap) "Recolher" else "Expandir"
                        )
                    }
                }

                if (isExpandedMap) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Filter chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedSectorFilter == 0,
                            onClick = { selectedSectorFilter = 0 },
                            label = { Text("Todos (${memoryMap.sectors.size})", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedSectorFilter == 1,
                            onClick = { selectedSectorFilter = 1 },
                            label = { Text("Autenticados (${memoryMap.authenticatedSectorsCount})", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = selectedSectorFilter == 2,
                            onClick = { selectedSectorFilter = 2 },
                            label = { Text("Falhas (${memoryMap.sectorCount - memoryMap.authenticatedSectorsCount})", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notice on offline diagnostic keys
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Diagnóstico 100% Offline: Autenticação testada exclusivamente com chaves padrão conhecidas de fábrica/diagnóstico. Nenhuma chave é extraída da tag.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val filteredSectors = when (selectedSectorFilter) {
                        1 -> memoryMap.sectors.filter { it.status == com.nfcinspector.app.data.model.MifareSectorStatus.READ_SUCCESS || it.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_A || it.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_B || it.status == com.nfcinspector.app.data.model.MifareSectorStatus.PARTIAL_READ }
                        2 -> memoryMap.sectors.filter { it.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_FAILED || it.status == com.nfcinspector.app.data.model.MifareSectorStatus.NOT_TESTED }
                        else -> memoryMap.sectors
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        filteredSectors.forEach { sector ->
                            MifareSectorCard(
                                sector = sector,
                                onCopyBlock = { blockLabel, hex ->
                                    copyToClipboard(context, blockLabel, hex)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MifareSectorCard(
    sector: com.nfcinspector.app.data.model.MifareSectorData,
    onCopyBlock: (String, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.READ_SUCCESS || sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_A || sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_B || sector.sectorIndex == 0) }

    val statusColor = when (sector.status) {
        com.nfcinspector.app.data.model.MifareSectorStatus.READ_SUCCESS -> SignalGreen
        com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_A -> TechBlue
        com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_B -> TechBlue
        com.nfcinspector.app.data.model.MifareSectorStatus.PARTIAL_READ -> WarningAmber
        com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_FAILED -> MaterialTheme.colorScheme.onSurfaceVariant
        com.nfcinspector.app.data.model.MifareSectorStatus.NOT_TESTED -> MaterialTheme.colorScheme.outline
    }

    val statusBg = statusColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val secFormatted = String.format(Locale.US, "Setor %02d", sector.sectorIndex)
                    Text(
                        text = secFormatted,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${sector.blockCount} blocos: #${sector.firstBlockIndex}..#${sector.firstBlockIndex + sector.blockCount - 1})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusBg
                ) {
                    Text(
                        text = sector.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (sector.authKeyType != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Autenticação: ${sector.authKeyType}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = SignalGreen
                        )
                        if (sector.authKeyName != null) {
                            Text(
                                text = " • Chave: ${sector.authKeyName}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    val resultado = if (sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.READ_SUCCESS || sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_A || sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.AUTH_KEY_B || sector.status == com.nfcinspector.app.data.model.MifareSectorStatus.PARTIAL_READ) "sucesso" else sector.status.label
                    Text(
                        text = "Resultado: $resultado",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = if (resultado == "sucesso") SignalGreen else WarningAmber
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sector.blocks.forEach { block ->
                        MifareBlockItem(
                            block = block,
                            onCopy = { onCopyBlock("Bloco ${block.blockIndex}", block.hexFormatted) }
                        )
                    }
                }

                // Access bits interpretation if available
                sector.accessBits?.let { ab ->
                    Spacer(modifier = Modifier.height(10.dp))
                    MifareAccessBitsView(ab)
                }
            }
        }
    }
}

@Composable
fun MifareBlockItem(
    block: com.nfcinspector.app.data.model.MifareBlockData,
    onCopy: () -> Unit
) {
    val (typeBadgeColor, typeBadgeBg) = when (block.blockType) {
        com.nfcinspector.app.data.model.MifareBlockType.MANUFACTURER -> WarningAmber to WarningAmber.copy(alpha = 0.15f)
        com.nfcinspector.app.data.model.MifareBlockType.DATA -> TechBlue to TechBlue.copy(alpha = 0.15f)
        com.nfcinspector.app.data.model.MifareBlockType.SECTOR_TRAILER -> Color(0xFF8B5CF6) to Color(0xFF8B5CF6).copy(alpha = 0.15f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val blkLabel = String.format(Locale.US, "Bloco %02d", block.blockIndex)
                    Text(
                        text = blkLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = typeBadgeBg
                    ) {
                        Text(
                            text = block.blockType.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = typeBadgeColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                if (block.isReadSuccess) {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copiar Hex do Bloco",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (block.isReadSuccess) {
                Text(
                    text = "HEX:   " + block.hexFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ASCII: " + block.asciiFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "[Não lido / Protegido por Chave]",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun MifareAccessBitsView(accessBits: com.nfcinspector.app.data.model.MifareAccessBits) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interpretação dos Access Bits",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (accessBits.isValid) SignalGreen.copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (accessBits.isValid) "Bits Válidos" else "Bits Inconsistentes",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (accessBits.isValid) SignalGreen else Color(0xFFEF4444),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bytes (6,7,8): ${accessBits.rawBytesHex}  |  GPB (Byte 9): ${accessBits.gpbHex ?: "N/A"}",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (accessBits.isValid) {
                accessBits.trailerPermissions?.let { tp ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Sector Trailer (C1=${tp.c1}, C2=${tp.c2}, C3=${tp.c3}):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    Text(
                        text = "  Key A Read: ${tp.keyARead} | Key A Write: ${tp.keyAWrite}\n  Access Bits Read: ${tp.accessBitsRead} | Access Bits Write: ${tp.accessBitsWrite}\n  Key B Read: ${tp.keyBRead} | Key B Write: ${tp.keyBWrite}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                accessBits.blockPermissions.forEach { bp ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• ${bp.blockRangeLabel} (C1=${bp.c1}, C2=${bp.c2}, C3=${bp.c3}):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                    Text(
                        text = "  Leitura: ${bp.readAccess} | Escrita: ${bp.writeAccess}\n  Incremento: ${bp.incrementAccess} | Decremento/Transfer/Restore: ${bp.decrementTransferRestoreAccess}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                accessBits.inconsistencyError?.let { err ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "⚠ $err",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = Color(0xFFEF4444)
                    )
                }
            }
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
