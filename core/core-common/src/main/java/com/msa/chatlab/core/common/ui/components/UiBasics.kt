package com.msa.chatlab.core.common.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.msa.chatlab.core.common.ui.theme.LocalSpacing

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val s = LocalSpacing.current
    ElevatedCard(modifier) {
        Column(Modifier.fillMaxWidth().padding(s.lg), verticalArrangement = Arrangement.spacedBy(s.md)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (trailing != null) Row(content = trailing)
            }
            content()
        }
    }
}

@Composable
fun StatusPill(text: String, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val s = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxWidth().padding(s.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(s.md)
    ) {
        Icon(icon, contentDescription = null)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
