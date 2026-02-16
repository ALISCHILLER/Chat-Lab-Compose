package com.msa.chatlab.feature.settings.screen

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun ExportDialog(
    text: String,
    onClose: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = {
                clipboard.setText(AnnotatedString(text))
            }) { Text("Copy") }
        },
        dismissButton = {
            TextButton(onClick = onClose) { Text("Close") }
        },
        title = { Text("Export JSON") },
        text = { Text(text) }
    )
}
