package com.msa.chatlab.core.common.ui.insets

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union

// برای Scaffold محتوا
val AppContentInsets: WindowInsets
    get() = WindowInsets.safeDrawing

// برای composer پایین صفحه که با کیبورد بالا بیاد
val AppImeInsets: WindowInsets
    get() = WindowInsets.safeDrawing.union(WindowInsets.ime)
