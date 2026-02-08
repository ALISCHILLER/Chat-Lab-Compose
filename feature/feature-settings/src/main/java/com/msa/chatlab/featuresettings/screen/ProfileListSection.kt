package com.msa.chatlab.featuresettings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.featuresettings.state.SettingsUiEvent
import com.msa.chatlab.featuresettings.state.SettingsUiState

@Composable
fun ProfileListSection(
    state: SettingsUiState,
    onEvent: (SettingsUiEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Text("Settings", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = state.searchQuery,
                onValueChange = { onEvent(SettingsUiEvent.SearchChanged(it)) },
                label = { Text("Search") }
            )
            Button(onClick = { onEvent(SettingsUiEvent.CreateNew) }) {
                Text("Create")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { onEvent(SettingsUiEvent.OpenImport) }) { Text("Import") }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.cards, key = { it.id }) { card ->
                ProfileCard(
                    card = card,
                    onApply = { onEvent(SettingsUiEvent.Apply(card.id)) },
                    onEdit = { onEvent(SettingsUiEvent.Edit(card.id)) },
                    onDuplicate = { onEvent(SettingsUiEvent.Duplicate(card.id)) },
                    onExport = { onEvent(SettingsUiEvent.Export(card.id)) },
                    onDelete = { onEvent(SettingsUiEvent.Delete(card.id)) }
                )
            }
        }
    }
}
