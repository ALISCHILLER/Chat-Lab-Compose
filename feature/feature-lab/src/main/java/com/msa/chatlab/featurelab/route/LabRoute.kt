package com.msa.chatlab.featurelab.route

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.msa.chatlab.featurelab.screen.LabScreen
import com.msa.chatlab.featurelab.state.LabUiEffect
import com.msa.chatlab.featurelab.vm.LabViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun LabRoute(padding: PaddingValues) {
    val vm: LabViewModel = koinViewModel()
    val state = vm.uiState.collectAsState()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.uiEffect.collectLatest {
            when (it) {
                is LabUiEffect.ShowSnackbar -> {
                    Toast.makeText(ctx, it.message, Toast.LENGTH_SHORT).show()
                }
                is LabUiEffect.ShowExportDialog -> {
                    // Here you would open a dialog to show the export files
                    Toast.makeText(ctx, "Export bundle created", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    LabScreen(
        state = state.value,
        onEvent = vm::onEvent,
        padding = padding
    )
}
