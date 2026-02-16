package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ImportDialog(
    text: String,
    onTextChange: (String) -> Unit,
    onClose: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onImport) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Cancel") }
        },
        title = { Text("Import JSON") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                label = { Text("Paste profile JSON here") }
            )
        }
    )
}
