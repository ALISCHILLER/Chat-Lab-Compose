package com.msa.chatlab.feature.chat.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import com.msa.chatlab.core.designsystem.component.EmptyState
import com.msa.chatlab.core.designsystem.component.SectionCard
import com.msa.chatlab.core.designsystem.theme.LocalSpacing
import com.msa.chatlab.core.domain.model.ConversationRow
import com.msa.chatlab.feature.chat.vm.ChatListViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatListRoute(
    padding: PaddingValues,
    onOpen: (String) -> Unit,
) {
    val vm: ChatListViewModel = koinViewModel()
    val st by vm.state.collectAsState()
    ChatListScreen(
        padding = padding,
        query = st.query,
        profileName = st.profileName,
        items = st.items,
        onQuery = vm::onQuery,
        onOpen = onOpen
    )
}

@Composable
private fun ChatListScreen(
    padding: PaddingValues,
    query: String,
    profileName: String,
    items: List<ConversationRow>,
    onQuery: (String) -> Unit,
    onOpen: (String) -> Unit,
) {
    val s = LocalSpacing.current

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.spacedBy(s.lg)
    ) {
        SectionCard(title = "Chats") {
            Text("Profile: $profileName", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search destinations / last messages…") }
            )
        }

        if (items.isEmpty()) {
            EmptyState(
                title = "No conversations",
                message = "Send a message to create a conversation.",
                icon = Icons.Default.ChatBubble
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(s.sm)) {
                items(items, key = { it.destination }) { row ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(row.destination) }
                    ) {
                        Column(Modifier.padding(s.lg), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(row.destination, style = MaterialTheme.typography.titleMedium)
                            Text(
                                row.lastText ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Total: ${row.total} • Last: ${row.lastStatus ?: "-"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
