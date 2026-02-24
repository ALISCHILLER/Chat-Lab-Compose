package com.msa.chatlab.feature.settings.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.msa.chatlab.core.designsystem.theme.LocalSpacing
import com.msa.chatlab.feature.settings.state.ProfileEditorUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorSheet(
    ui: ProfileEditorUi,
    onChange: (ProfileEditorUi) -> Unit,
    onSave: () -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val s = LocalSpacing.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(s.lg),
            verticalArrangement = Arrangement.spacedBy(s.md)
        ) {
            Text(if (ui.id == null) "New Profile" else "Edit Profile", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = ui.name,
                onValueChange = { onChange(ui.copy(name = it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.protocol,
                onValueChange = { onChange(ui.copy(protocol = it)) },
                label = { Text("Protocol") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.endpoint,
                onValueChange = { onChange(ui.copy(endpoint = it)) },
                label = { Text("Endpoint") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ui.destinationDefault,
                onValueChange = { onChange(ui.copy(destinationDefault = it)) },
                label = { Text("Default destination") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            Text("Retry Policy", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(s.sm)) {
                OutlinedTextField(
                    value = ui.retryMaxAttempts.toString(),
                    onValueChange = { onChange(ui.copy(retryMaxAttempts = it.toIntOrNull() ?: ui.retryMaxAttempts)) },
                    label = { Text("maxAttempts") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ui.retryInitialMs.toString(),
                    onValueChange = { onChange(ui.copy(retryInitialMs = it.toLongOrNull() ?: ui.retryInitialMs)) },
                    label = { Text("initialMs") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = ui.retryMaxMs.toString(),
                    onValueChange = { onChange(ui.copy(retryMaxMs = it.toLongOrNull() ?: ui.retryMaxMs)) },
                    label = { Text("maxMs") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = ui.chaosEnabled, onCheckedChange = { onChange(ui.copy(chaosEnabled = it)) })
                Spacer(Modifier.width(s.sm))
                Text("Chaos mode")
            }

            AnimatedVisibility(visible = ui.chaosEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(s.sm)) {
                    OutlinedTextField(
                        value = ui.chaosDropPercent.toString(),
                        onValueChange = { onChange(ui.copy(chaosDropPercent = it.toDoubleOrNull() ?: ui.chaosDropPercent)) },
                        label = { Text("dropRatePercent") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(s.sm)) {
                        OutlinedTextField(
                            value = ui.chaosDelayMinMs.toString(),
                            onValueChange = { onChange(ui.copy(chaosDelayMinMs = it.toLongOrNull() ?: ui.chaosDelayMinMs)) },
                            label = { Text("delayMinMs") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ui.chaosDelayMaxMs.toString(),
                            onValueChange = { onChange(ui.copy(chaosDelayMaxMs = it.toLongOrNull() ?: ui.chaosDelayMaxMs)) },
                            label = { Text("delayMaxMs") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(s.sm)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save") }
            }

            if (onDelete != null) {
                TextButton(onClick = onDelete) { Text("Delete profile", color = MaterialTheme.colorScheme.error) }
            }

            Spacer(Modifier.height(s.xl))
        }
    }
}
