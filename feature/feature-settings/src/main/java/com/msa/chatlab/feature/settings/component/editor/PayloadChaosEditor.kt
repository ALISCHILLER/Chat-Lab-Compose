package com.msa.chatlab.feature.settings.component.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ChaosProfile
import com.msa.chatlab.core.domain.model.PayloadProfile

@Composable
fun PayloadChaosEditor(
    payloadProfile: PayloadProfile,
    chaosProfile: ChaosProfile,
    onPayloadChanged: (PayloadProfile) -> Unit,
    onChaosChanged: (ChaosProfile) -> Unit
) {
    var targetSize by remember(payloadProfile.targetSizeBytes) { mutableStateOf(payloadProfile.targetSizeBytes.toString()) }
    var dropRate by remember(chaosProfile.dropRatePercent) { mutableStateOf(chaosProfile.dropRatePercent.toString()) }
    var enabled by remember(chaosProfile.enabled) { mutableStateOf(chaosProfile.enabled) }

    LaunchedEffect(targetSize) {
        onPayloadChanged(
            payloadProfile.copy(
                targetSizeBytes = targetSize.toIntOrNull() ?: 1024
            )
        )
    }

    LaunchedEffect(dropRate, enabled) {
        onChaosChanged(
            chaosProfile.copy(
                enabled = enabled,
                dropRatePercent = dropRate.toDoubleOrNull() ?: 0.0
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Payload & Chaos Settings", style = MaterialTheme.typography.titleMedium)

        Column {
            Text("Payload", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = targetSize,
                onValueChange = { targetSize = it.filter { c -> c.isDigit() } },
                label = { Text("Target size (bytes)") },
                placeholder = { Text("1024") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = enabled,
                    onCheckedChange = { enabled = it }
                )
                Text("Enable chaos (packet loss/simulated disconnects)")
            }

            if (enabled) {
                OutlinedTextField(
                    value = dropRate,
                    onValueChange = { dropRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Drop rate (%)") },
                    placeholder = { Text("5.0") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Warning: Chaos mode is for lab testing only — not for production!",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
