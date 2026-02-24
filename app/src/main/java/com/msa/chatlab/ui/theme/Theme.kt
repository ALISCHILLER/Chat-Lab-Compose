package com.msa.chatlab.ui.theme

import androidx.compose.runtime.Composable

/**
 * ✅ Dedup: keep app.ui.theme as a thin wrapper to core-designsystem.
 */
@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.theme.ChatLabTheme",
    replaceWith = ReplaceWith("ChatLabTheme(darkTheme, dynamicColor, content)", "com.msa.chatlab.core.designsystem.theme.ChatLabTheme")
)
@Composable
fun ChatLabTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) = com.msa.chatlab.core.designsystem.theme.ChatLabTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
)
