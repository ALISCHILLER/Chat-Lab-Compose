package com.msa.chatlab.featuresettings.route

import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.msa.chatlab.featuresettings.screen.SettingsScreen
import com.msa.chatlab.featuresettings.state.SettingsUiEffect
import com.msa.chatlab.featuresettings.vm.SettingsViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsRoute(padding: PaddingValues) {
    val vm: SettingsViewModel = koinViewModel()
    val ctx = LocalContext.current

    LaunchedEffect(Unit) {
        vm.effects.collectLatest { eff ->
            when (eff) {
                is SettingsUiEffect.Toast -> Toast.makeText(ctx, eff.message, Toast.LENGTH_SHORT).show()
                is SettingsUiEffect.ShowExport -> {
                    // فاز ۱: فقط Toast کوتاه + JSON رو تو لاگ/دیالوگ بعداً
                    Toast.makeText(ctx, "Export ready (copy from logs)", Toast.LENGTH_SHORT).show()
                    android.util.Log.d("ChatLab-Export", eff.json)
                }
            }
        }
    }

    SettingsScreen(
        state = vm.uiState.value,
        onEvent = vm::onEvent,
        padding = padding
    )
}
