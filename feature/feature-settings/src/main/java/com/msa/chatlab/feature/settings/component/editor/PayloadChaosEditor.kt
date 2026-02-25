package com.msa.chatlab.feature.settings.component.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ChaosPolicy
import com.msa.chatlab.core.domain.model.PayloadPolicy

@Composable
fun PayloadChaosEditor(
    payloadPolicy: PayloadPolicy,
    chaosPolicy: ChaosPolicy,
    onPayloadChanged: (PayloadPolicy) -> Unit,
    onChaosChanged: (ChaosPolicy) -> Unit
) {
    var percentage by remember(payloadPolicy.percentage) { mutableStateOf(payloadPolicy.percentage?.toString() ?: "") }
    var value by remember(payloadPolicy.value) { mutableStateOf(payloadPolicy.value ?: "") }
    var chaosPercentage by remember(chaosPolicy.percentage) { mutableStateOf(chaosPolicy.percentage?.toString() ?: "") }
    var enabled by remember { mutableStateOf(chaosPolicy.actions?.isNotEmpty() == true) }

    LaunchedEffect(percentage, value) {
        onPayloadChanged(
            payloadPolicy.copy(
                percentage = percentage.toIntOrNull(),
                value = value.takeIf { it.isNotBlank() }
            )
        )
    }

    LaunchedEffect(chaosPercentage, enabled) {
        onChaosChanged(
            chaosPolicy.copy(
                percentage = chaosPercentage.toIntOrNull(),
                actions = if (enabled) chaosPolicy.actions else emptyList()
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Payload & Chaos Settings", style = MaterialTheme.typography.titleMedium)

        Column {
            Text("Payload", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = percentage,
                onValueChange = { percentage = it.filter { c -> c.isDigit() } },
                label = { Text("Percentage") },
                placeholder = { Text("100") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") },
                placeholder = { Text("custom payload") },
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
                    value = chaosPercentage,
                    onValueChange = { chaosPercentage = it.filter { c -> c.isDigit() } },
                    label = { Text("Chaos Percentage (%)") },
                    placeholder = { Text("5") },
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
