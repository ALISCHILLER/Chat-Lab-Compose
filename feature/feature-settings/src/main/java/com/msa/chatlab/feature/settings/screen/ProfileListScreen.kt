package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.settings.component.common.ProfileCard
import com.msa.chatlab.feature.settings.state.SettingsUiEvent
import com.msa.chatlab.feature.settings.state.UiProfileCard

@Composable
fun ProfileListScreen(
    cards: List<UiProfileCard>,
    isLoading: Boolean,
    onEvent: (SettingsUiEvent) -> Unit,
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No profiles found. Create one to get started!")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 72.dp) // FAB space
    ) {
        items(cards) { card ->
            ProfileCard(
                profile = card,
                onEvent = onEvent
            )
        }
    }
}
