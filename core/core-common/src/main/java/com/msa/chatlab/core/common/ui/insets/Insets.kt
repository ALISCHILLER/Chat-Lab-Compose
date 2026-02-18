package com.msa.chatlab.core.common.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.runtime.Composable

// For Scaffold content
val AppContentInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing

// For composer at the bottom of the screen that moves with the keyboard
val AppImeInsets: WindowInsets
    @Composable
    get() = WindowInsets.safeDrawing.union(WindowInsets.ime)
