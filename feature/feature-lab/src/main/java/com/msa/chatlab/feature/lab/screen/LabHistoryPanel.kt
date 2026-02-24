package com.msa.chatlab.feature.lab.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.designsystem.component.SectionCard
import com.msa.chatlab.core.designsystem.theme.LocalSpacing
import com.msa.chatlab.core.storage.entity.RunEntity

@Composable
fun LabHistoryPanel(
    runs: List<RunEntity>,
    onOpenRun: (String) -> Unit
) {
    val s = LocalSpacing.current
    SectionCard(title = "Run history") {
        if (runs.isEmpty()) {
            Text("No runs yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            return@SectionCard
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
            verticalArrangement = Arrangement.spacedBy(s.sm)
        ) {
            items(runs, key = { it.id }) { r ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenRun(r.id) }
                ) {
                    Column(Modifier.padding(s.lg), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(r.scenarioPreset, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "protocol=${r.protocolType} • started=${r.startedAt}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
