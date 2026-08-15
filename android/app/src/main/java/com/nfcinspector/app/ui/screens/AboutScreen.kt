package com.nfcinspector.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nfcinspector.app.ui.theme.SignalGreen
import com.nfcinspector.app.ui.theme.TechBlue

@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // App Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "NFC Inspector",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = TechBlue)
                )
                Text(
                    text = "Versão 1.0.0 • Diagnóstico Técnico e Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ferramenta de diagnóstico, identificação e aprendizado sobre cartões e tags NFC pertencentes ao usuário ou sob autorização de teste.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Privacy Commitment Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SignalGreen.copy(alpha = 0.08f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint = SignalGreen, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("100% Offline e Privado", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SignalGreen))
                    Text(
                        "Sem internet, sem anúncios, sem rastreadores, sem telemetria. Todos os dados permanecem exclusivamente no seu aparelho.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Guia Conceitual de Tecnologias",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.Nfc,
            title = "NFC (Near Field Communication)",
            description = "Tecnologia de comunicação por proximidade operando na frequência padrão internacional de 13,56 MHz, derivada dos padrões ISO/IEC 14443 e ISO/IEC 15693."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.Fingerprint,
            title = "UID (Unique Identifier)",
            description = "Identificador de nível de link físico fornecido por determinadas tags/cartões (4, 7 ou 10 bytes). Um UID nunca deve ser considerado automaticamente como credencial segura de autenticação."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.FormatListBulleted,
            title = "NDEF (NFC Data Exchange Format)",
            description = "Formato binário padronizado pelo NFC Forum para armazenar e trocar informações interoperáveis em tags (Textos formatados, URLs, vCards, MIME Types)."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.CreditCard,
            title = "ISO-DEP (ISO 14443-4)",
            description = "Camada de protocolo de transmissão em blocos APDU utilizada para comunicação com cartões inteligentes (smart cards, bilhetes de transporte e ePassports)."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.Memory,
            title = "MIFARE® (Classic & Ultralight)",
            description = "Família de circuitos integrados sem contato da NXP. O suporte a MIFARE Classic exige hardware de RF compatível no chipset do smartphone; sua ausência não significa que o cartão não seja MIFARE."
        )

        Spacer(modifier = Modifier.height(10.dp))

        AboutTopicCard(
            icon = Icons.Outlined.Security,
            title = "Cartões Bancários & Diagnóstico Seguro",
            description = "O aplicativo detecta a camada de RF e tecnologias ISO-DEP de cartões sem contato exclusivamente para diagnóstico. O NFC Inspector não obtém CVV, PIN, chaves criptográficas nem dados financeiros confidenciais."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AboutTopicCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = TechBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
