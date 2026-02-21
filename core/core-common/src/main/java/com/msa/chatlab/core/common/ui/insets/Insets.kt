package com.msa.chatlab.core.common.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

// For Scaffold content
val AppContentInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing

// For the composer at the bottom of the screen to move up with the keyboard
val AppImeInsets: WindowInsets
    @Composable
    get() = WindowInsets.ime
