package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.settings.state.UiProfileCard

@Composable
fun ProfileCard(profile: UiProfileCard, onEvent: (com.msa.chatlab.feature.settings.state.SettingsUiEvent) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvent(com.msa.chatlab.feature.settings.state.SettingsUiEvent.SelectProfile(profile.id)) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = profile.name, style = MaterialTheme.typography.titleMedium)
                Text(text = profile.protocol, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = profile.isActive,
                onCheckedChange = { onEvent(com.msa.chatlab.feature.settings.state.SettingsUiEvent.ToggleProfile(profile.id, it)) }
            )
            IconButton(onClick = { onEvent(com.msa.chatlab.feature.settings.state.SettingsUiEvent.EditProfile(profile.id)) }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
            }
        }
    }
}
