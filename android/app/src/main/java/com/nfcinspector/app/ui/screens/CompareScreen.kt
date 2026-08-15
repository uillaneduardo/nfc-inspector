package com.nfcinspector.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.ui.theme.*
import com.nfcinspector.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    viewModel: MainViewModel,
    onSelectTag: (slot: Int) -> Unit
) {
    val tag1 by viewModel.compareTag1.collectAsState()
    val tag2 by viewModel.compareTag2.collectAsState()
    val allHistory by viewModel.historyScans.collectAsState()

    var showPickerSlot by remember { mutableStateOf<Int?>(null) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Comparar Leituras NFC",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Selecione duas leituras do histórico para analisar divergências de hardware e protocolo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Slots Selector
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CompareSlotCard(
                title = "Leitura A",
                tag = tag1,
                modifier = Modifier.weight(1f),
                onClick = { showPickerSlot = 1 }
            )
            CompareSlotCard(
                title = "Leitura B",
                tag = tag2,
                modifier = Modifier.weight(1f),
                onClick = { showPickerSlot = 2 }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (tag1 != null && tag2 != null) {
            Text(
                text = "Diferencial Técnico",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(10.dp))

            DiffSection(
                title = "Identificador (UID)",
                rows = listOf(
                    DiffItem("UID Hex", tag1!!.uidColonHex, tag2!!.uidColonHex),
                    DiffItem("Comprimento", "${tag1!!.uidLengthBytes} bytes", "${tag2!!.uidLengthBytes} bytes"),
                    DiffItem("Decimal", tag1!!.uidDecimal, tag2!!.uidDecimal)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            DiffSection(
                title = "Tecnologias & Arquitetura",
                rows = listOf(
                    DiffItem("Tecnologia Principal", tag1!!.mainTechnology, tag2!!.mainTechnology),
                    DiffItem("Tech List", tag1!!.technologies.sorted().joinToString(", "), tag2!!.technologies.sorted().joinToString(", "))
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // NFC-A Diff
            if (tag1!!.nfcA != null || tag2!!.nfcA != null) {
                DiffSection(
                    title = "Parâmetros NFC-A",
                    rows = listOf(
                        DiffItem("ATQA", tag1!!.nfcA?.atqaHex ?: "N/A", tag2!!.nfcA?.atqaHex ?: "N/A"),
                        DiffItem("SAK", tag1!!.nfcA?.sakHex ?: "N/A", tag2!!.nfcA?.sakHex ?: "N/A"),
                        DiffItem("Max Transceive", tag1!!.nfcA?.maxTransceiveBytes?.toString() ?: "N/A", tag2!!.nfcA?.maxTransceiveBytes?.toString() ?: "N/A")
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ISO-DEP Diff
            if (tag1!!.isoDep != null || tag2!!.isoDep != null) {
                DiffSection(
                    title = "Parâmetros ISO-DEP",
                    rows = listOf(
                        DiffItem("Historical Bytes", tag1!!.isoDep?.historicalBytesHex ?: "N/A", tag2!!.isoDep?.historicalBytesHex ?: "N/A"),
                        DiffItem("Extended APDU", tag1!!.isoDep?.isExtendedLengthApduSupported?.toString() ?: "N/A", tag2!!.isoDep?.isExtendedLengthApduSupported?.toString() ?: "N/A")
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // NDEF Diff
            if (tag1!!.ndef != null || tag2!!.ndef != null) {
                DiffSection(
                    title = "Parâmetros NDEF",
                    rows = listOf(
                        DiffItem("Formato NDEF", tag1!!.ndef?.typeName ?: "N/A", tag2!!.ndef?.typeName ?: "N/A"),
                        DiffItem("Gravável", tag1!!.ndef?.isWritable?.toString() ?: "N/A", tag2!!.ndef?.isWritable?.toString() ?: "N/A"),
                        DiffItem("Tamanho Atual", "${tag1!!.ndef?.currentSizeBytes ?: 0} B", "${tag2!!.ndef?.currentSizeBytes ?: 0} B"),
                        DiffItem("Capacidade Máx", "${tag1!!.ndef?.maxSizeBytes ?: 0} B", "${tag2!!.ndef?.maxSizeBytes ?: 0} B"),
                        DiffItem("Qtd Registros", "${tag1!!.ndef?.recordCount ?: 0}", "${tag2!!.ndef?.recordCount ?: 0}")
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecione duas leituras acima para visualizar a tabela comparativa de propriedades técnicas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal Sheet or Dialog to pick tag from history
    showPickerSlot?.let { slot ->
        ModalBottomSheet(
            onDismissRequest = { showPickerSlot = null }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Selecionar leitura para Slot $slot",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (allHistory.isEmpty()) {
                    Text("Nenhum histórico disponível para seleção.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    allHistory.forEach { scan ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                viewModel.selectForCompare(slot, scan)
                                showPickerSlot = null
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(scan.uidColonHex, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Text(scan.mainTechnology, fontSize = 12.sp, color = TechBlue)
                                }
                                Text(scan.formattedDateTime, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class DiffItem(val name: String, val valA: String, val valB: String)

@Composable
fun DiffSection(title: String, rows: List<DiffItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = TechBlue)
            )
            Spacer(modifier = Modifier.height(8.dp))
            rows.forEach { item ->
                val isEqual = item.valA.trim() == item.valB.trim()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.weight(1.2f),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.valA,
                        modifier = Modifier.weight(1.4f),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (isEqual) MaterialTheme.colorScheme.onSurface else ErrorRed
                    )
                    Text(
                        text = item.valB,
                        modifier = Modifier.weight(1.4f),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = if (isEqual) MaterialTheme.colorScheme.onSurface else SignalGreen
                    )
                }
            }
        }
    }
}

@Composable
fun CompareSlotCard(
    title: String,
    tag: TagRecord?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = TechBlue, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            if (tag != null) {
                Text(
                    text = tag.uidColonHex,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = tag.mainTechnology,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    maxLines = 1
                )
            } else {
                Text("Toque para escolher", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
