package com.msa.chatlab.core.common.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ✅ Dedup: canonical components live in core-designsystem.
 * Keeping these wrappers to avoid breaking existing imports.
 */
@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.component.SectionCard",
    replaceWith = ReplaceWith(
        "SectionCard(title, modifier, trailing, content)",
        "com.msa.chatlab.core.designsystem.component.SectionCard"
    )
)
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = com.msa.chatlab.core.designsystem.component.SectionCard(
    title = title,
    modifier = modifier,
    trailing = trailing,
    content = content
)

@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.component.StatusPill",
    replaceWith = ReplaceWith(
        "StatusPill(text, modifier)",
        "com.msa.chatlab.core.designsystem.component.StatusPill"
    )
)
@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) =
    com.msa.chatlab.core.designsystem.component.StatusPill(text = text, modifier = modifier)

@Deprecated(
    message = "Use com.msa.chatlab.core.designsystem.component.EmptyState",
    replaceWith = ReplaceWith(
        "EmptyState(title, message, modifier, icon, actionLabel, onAction)",
        "com.msa.chatlab.core.designsystem.component.EmptyState"
    )
)
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) = com.msa.chatlab.core.designsystem.component.EmptyState(
    title = title,
    message = message,
    modifier = modifier,
    icon = icon,
    actionLabel = actionLabel,
    onAction = onAction
)
