package com.nfcinspector.app.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.ui.theme.ErrorRed
import com.nfcinspector.app.ui.theme.TechBlue
import com.nfcinspector.app.ui.viewmodel.MainViewModel

@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onViewReport: (TagRecord) -> Unit,
    onNavigateToCompare: () -> Unit
) {
    val historyScans by viewModel.historyScans.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Histórico Local",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${historyScans.size} leituras armazenadas offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (historyScans.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        Icons.Default.DeleteSweep,
                        contentDescription = "Limpar Todo o Histórico",
                        tint = ErrorRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (historyScans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhuma leitura salva ainda",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aproxime e inspecione uma tag na aba 'Leitor' e toque em 'Salvar leitura' para armazená-la offline.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(historyScans, key = { it.id }) { scan ->
                    HistoryItemCard(
                        tag = scan,
                        onView = { onViewReport(scan) },
                        onDelete = { viewModel.deleteScan(scan.id) },
                        onCompareSlot1 = {
                            viewModel.selectForCompare(1, scan)
                            onNavigateToCompare()
                        },
                        onCompareSlot2 = {
                            viewModel.selectForCompare(2, scan)
                            onNavigateToCompare()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Excluir Todo o Histórico?") },
            text = { Text("Todas as leituras locais serão apagadas permanentemente deste dispositivo.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    showClearDialog = false
                }) {
                    Text("Excluir Tudo", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun HistoryItemCard(
    tag: TagRecord,
    onView: () -> Unit,
    onDelete: () -> Unit,
    onCompareSlot1: () -> Unit,
    onCompareSlot2: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onView() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        fontSize = 16.sp
                    )
                )
                Text(
                    text = tag.formattedDateTime,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tag.mainTechnology,
                style = MaterialTheme.typography.bodySmall.copy(color = TechBlue, fontWeight = FontWeight.Medium)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                tag.technologies.take(4).forEach { tech ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tech,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    TextButton(onClick = onCompareSlot1) {
                        Text("Comp A", fontSize = 12.sp)
                    }
                    TextButton(onClick = onCompareSlot2) {
                        Text("Comp B", fontSize = 12.sp)
                    }
                }
                Row {
                    IconButton(onClick = onView) {
                        Icon(Icons.Outlined.Description, contentDescription = "Ver Relatório", tint = TechBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = ErrorRed)
                    }
                }
            }
        }
    }
}
