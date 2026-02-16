package com.msa.chatlab.feature.settings.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.ProtocolType
import com.msa.chatlab.core.domain.value.ProfileId
import com.msa.chatlab.feature.settings.component.common.ProfileCard
import com.msa.chatlab.feature.settings.state.ProfileListItem

@Composable
fun ProfileListScreen(
    profiles: List<ProfileListItem>,
    activeProfileId: ProfileId?,
    isLoading: Boolean,
    onEditProfile: (ProfileId) -> Unit,
    onDeleteProfile: (ProfileId) -> Unit,
    onActivateProfile: (ProfileId) -> Unit
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (profiles.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No profiles found. Create one to get started!")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 72.dp) // فضای FAB
    ) {
        items(profiles) { profile ->
            ProfileCard(
                profile = profile,
                isActive = profile.id == activeProfileId,
                onEdit = { onEditProfile(profile.id) },
                onDelete = { onDeleteProfile(profile.id) },
                onActivate = { onActivateProfile(profile.id) }
            )
        }
    }
}

@Composable
fun ProtocolBadge(type: ProtocolType) {
    val (text, color) = when (type) {
        ProtocolType.WS_OKHTTP -> "WS (OkHttp)" to Color(0xFF2196F3)
        ProtocolType.WS_KTOR -> "WS (Ktor)" to Color(0xFF4CAF50)
        ProtocolType.MQTT -> "MQTT" to Color(0xFFFF9800)
        ProtocolType.SOCKETIO -> "Socket.IO" to Color(0xFF9C27B0)
        ProtocolType.SIGNALR -> "SignalR" to Color(0xFFE91E63)
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f),
        contentColor = color
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
