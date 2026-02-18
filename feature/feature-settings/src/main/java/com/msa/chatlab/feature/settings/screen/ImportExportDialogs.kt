package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.common.ui.theme.LocalSpacing
import com.msa.chatlab.feature.settings.state.ImportExportUi

@Composable
fun ImportDialog(
    ui: ImportExportUi,
    onJsonChange: (String) -> Unit,
    onImport: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalSpacing.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Profiles") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(s.sm)) {
                ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                OutlinedTextField(
                    value = ui.json,
                    onValueChange = onJsonChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 520.dp),
                    placeholder = { Text("Paste exported JSON here…") }
                )
            }
        },
        confirmButton = { Button(onClick = onImport) { Text("Import") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
fun ExportDialog(
    ui: ImportExportUi,
    onDismiss: () -> Unit
) {
    val s = LocalSpacing.current
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Profiles") },
        text = {
            OutlinedTextField(
                value = ui.json,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp, max = 520.dp)
            )
        },
        confirmButton = {
            Button(onClick = { clipboard.setText(AnnotatedString(ui.json)) }) { Text("Copy") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
