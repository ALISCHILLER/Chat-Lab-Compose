package com.msa.chatlab.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal

@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.theme.Spacing",
    replaceWith = ReplaceWith("Spacing", "com.msa.chatlab.core.designsystem.theme.Spacing")
)
typealias Spacing = com.msa.chatlab.core.designsystem.theme.Spacing

@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.theme.LocalSpacing",
    replaceWith = ReplaceWith("LocalSpacing", "com.msa.chatlab.core.designsystem.theme.LocalSpacing")
)
val LocalSpacing: ProvidableCompositionLocal<Spacing>
    get() = com.msa.chatlab.core.designsystem.theme.LocalSpacing

@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.theme.ProvideSpacing",
    replaceWith = ReplaceWith("ProvideSpacing(content)", "com.msa.chatlab.core.designsystem.theme.ProvideSpacing")
)
@Composable
fun ProvideSpacing(content: @Composable () -> Unit) =
    com.msa.chatlab.core.designsystem.theme.ProvideSpacing(content)
