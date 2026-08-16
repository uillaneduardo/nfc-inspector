package com.nfcinspector.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Layers
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
import com.nfcinspector.app.report.ReportExportHelper
import com.nfcinspector.app.report.ReportFormatter
import com.nfcinspector.app.ui.screens.report.ReportVisualView
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Visual, 1: Texto (TXT), 2: JSON

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

    // Storage Access Framework (SAF) launcher for exporting JSON file
    val createJsonDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            ReportExportHelper.saveContentToUri(context, uri, jsonExportText)
        }
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

            // Tab Selector: Visual vs Texto vs JSON
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = TechBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Layers, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Visual", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("Texto", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Outlined.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text("JSON", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Bar (Contextual per tab)
            when (selectedTab) {
                0 -> {
                    // Visual Tab Actions
                    val isSaved by viewModel.isCurrentTagSaved.collectAsState()
                    val isSaving by viewModel.isSaving.collectAsState()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                ReportExportHelper.shareReportFile(
                                    context = context,
                                    tag = tag,
                                    content = fullReportText,
                                    extension = "txt",
                                    mimeType = "text/plain",
                                    chooserTitle = "Compartilhar Relatório Técnico NFC"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar Relatório", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

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
                            modifier = Modifier.weight(0.7f),
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
                                Text("Salvo", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Outlined.Save, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salvar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                1 -> {
                    // TXT Tab Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NFC Inspector TXT", fullReportText))
                                Toast.makeText(context, "Relatório TXT copiado!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copiar TXT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                ReportExportHelper.shareReportFile(
                                    context = context,
                                    tag = tag,
                                    content = fullReportText,
                                    extension = "txt",
                                    mimeType = "text/plain",
                                    chooserTitle = "Compartilhar Relatório TXT"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compartilhar TXT", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                2 -> {
                    // JSON Tab Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Copiar JSON
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("NFC Inspector JSON", jsonExportText))
                                Toast.makeText(context, "JSON copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TechBlue)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copiar JSON", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Salvar Arquivo JSON via SAF
                        Button(
                            onClick = {
                                val suggestedFileName = ReportFormatter.getExportFileName(tag, "json")
                                createJsonDocLauncher.launch(suggestedFileName)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar JSON", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }

                        // Compartilhar JSON
                        Button(
                            onClick = {
                                ReportExportHelper.shareReportFile(
                                    context = context,
                                    tag = tag,
                                    content = jsonExportText,
                                    extension = "json",
                                    mimeType = "application/json",
                                    chooserTitle = "Exportar JSON NFC Inspector"
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Compartilhar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Tab Content
            when (selectedTab) {
                0 -> {
                    // Visual View
                    ReportVisualView(tag = tag)
                }
                1 -> {
                    // TXT Monospaced View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = fullReportText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
                2 -> {
                    // JSON Monospaced View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = jsonExportText,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
