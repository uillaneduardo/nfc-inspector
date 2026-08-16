package com.nfcinspector.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcinspector.app.data.model.TagRecord
import com.nfcinspector.app.report.ReportFormatter
import com.nfcinspector.app.ui.theme.CardBackground
import com.nfcinspector.app.ui.theme.SuccessGreen
import com.nfcinspector.app.ui.theme.TechBlue
import com.nfcinspector.app.ui.theme.WarningAmber
import com.nfcinspector.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    tag: TagRecord,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0: Relatório V2, 1: Exportação JSON
    val fullReportText = remember(tag) { tag.generateFullReport() }
    val jsonExportText = remember(tag) { tag.generateJsonExport() }
    val scrollState = rememberScrollState()

    val inspectionStatus = remember(tag) { ReportFormatter.determineInspectionStatus(tag) }
    val statusLabel = when (inspectionStatus) {
        "complete" -> "Inspeção Completa"
        "partial" -> "Inspeção Parcial"
        else -> "Identificação Estrutural"
    }
    val statusColor = when (inspectionStatus) {
        "complete" -> SuccessGreen
        "partial" -> WarningAmber
        else -> TechBlue
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório Técnico NFC", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Header Overview Badge Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = tag.uidColonHex,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${tag.mainTechnology} • ${tag.formattedDateTime}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AssistChip(
                        onClick = {},
                        label = { Text(statusLabel, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = statusColor) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = statusColor.copy(alpha = 0.12f)),
                        border = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Selector: Formatted vs JSON
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TechBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Relatório V2", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Outlined.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("JSON (Schema v1)", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Action
                Button(
                    onClick = {
                        val textToCopy = if (selectedTab == 0) fullReportText else jsonExportText
                        val label = if (selectedTab == 0) "NFC Inspector Relatório" else "NFC Inspector JSON"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(label, textToCopy))
                        val message = if (selectedTab == 0) "Relatório copiado para a área de transferência!" else "JSON copiado para a área de transferência!"
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                // Share Action
                Button(
                    onClick = {
                        val textToShare = if (selectedTab == 0) fullReportText else jsonExportText
                        val mimeType = if (selectedTab == 0) "text/plain" else "application/json"
                        val title = if (selectedTab == 0) "Compartilhar Relatório Técnico NFC" else "Exportar JSON NFC Inspector"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, textToShare)
                            putExtra(Intent.EXTRA_TITLE, tag.getExportFileName(if (selectedTab == 0) "txt" else "json"))
                            type = mimeType
                        }
                        val shareIntent = Intent.createChooser(sendIntent, title)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedTab == 0) "Compartilhar" else "Exportar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }

                // Save Action
                val isSaved by viewModel.isCurrentTagSaved.collectAsState()
                val isSaving by viewModel.isSaving.collectAsState()

                Button(
                    onClick = {
                        viewModel.saveCurrentScanManually { alreadySaved ->
                            if (alreadySaved) {
                                Toast.makeText(context, "Esta leitura já está salva no histórico local.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "✓ Leitura salva no histórico local!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isSaved && !isSaving,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = if (isSaved) {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    } else {
                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                ) {
                    if (isSaved) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salvo", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(Icons.Outlined.Save, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salvar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Formatted Mono Content Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = if (selectedTab == 0) fullReportText else jsonExportText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

