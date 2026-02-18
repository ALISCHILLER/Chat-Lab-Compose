package com.msa.chatlab.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.msa.chatlab.core.designsystem.theme.LocalSpacing

@Composable
fun ProfileCard(
    name: String,
    protocol: String,
    endpoint: String,
    isActive: Boolean,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    ElevatedCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(LocalSpacing.current.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    if (isActive) {
                        Spacer(Modifier.width(LocalSpacing.current.sm))
                        AssistChip(onClick = {}, label = { Text("Active") })
                    }
                }
                Spacer(Modifier.padding(LocalSpacing.current.xs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(onClick = {}, label = { Text(protocol) })
                    Text(endpoint, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
            }
        }
    }
}
