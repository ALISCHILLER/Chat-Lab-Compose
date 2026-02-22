package com.msa.chatlab.feature.lab.route

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.msa.chatlab.core.common.ui.UiMessenger
import com.msa.chatlab.feature.lab.screen.LabScreen
import com.msa.chatlab.feature.lab.state.LabUiEffect
import com.msa.chatlab.feature.lab.vm.LabViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@Composable
fun LabRoute(padding: PaddingValues) {
    val vm: LabViewModel = koinViewModel()
    val state by vm.uiState.collectAsState()
    val messenger: UiMessenger = get()
    val clipboard = LocalClipboardManager.current

    var export by remember { mutableStateOf<Map<String, String>?>(null) }

    LaunchedEffect(Unit) {
        vm.uiEffect.collectLatest { eff ->
            when (eff) {
                is LabUiEffect.ShowSnackbar -> messenger.trySnackbar(eff.message)
                is LabUiEffect.ShowExportDialog -> export = eff.files

                is LabUiEffect.CopyToClipboard -> {
                    clipboard.setText(AnnotatedString(eff.text))
                    messenger.trySnackbar("${eff.label} copied")
                }
            }
        }
    }

    LabScreen(state = state, onEvent = vm::onEvent, padding = padding)

    export?.let { files ->
        val combined = files.entries.joinToString("\n\n") { (name, content) ->
            "===== $name =====\n$content"
        }

        AlertDialog(
            onDismissRequest = { export = null },
            title = { Text("Export bundle") },
            text = {
                OutlinedTextField(
                    value = combined,
                    onValueChange = {},
                    readOnly = true,
                    minLines = 10
                )
            },
            confirmButton = {
                Button(onClick = { clipboard.setText(AnnotatedString(combined)) }) { Text("Copy all") }
            },
            dismissButton = {
                TextButton(onClick = { export = null }) { Text("Close") }
            }
        )
    }
}
