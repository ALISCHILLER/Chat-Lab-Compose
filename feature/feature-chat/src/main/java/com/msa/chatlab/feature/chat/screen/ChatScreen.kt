package com.msa.chatlab.feature.chat.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.chat.model.ChatMessageUi
import com.msa.chatlab.feature.chat.state.ChatUiState
import com.msa.chatlab.feature.chat.vm.OutboxUiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    chatState: ChatUiState,
    outboxState: OutboxUiState,
    input: String,
    destination: String,
    padding: PaddingValues,
    onInputChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onSend: () -> Unit,
    onClearError: () -> Unit,
    onRetryOutbox: () -> Unit,
    onClearOutbox: () -> Unit
) {
    var showOutboxSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Profile: ${chatState.profileName}")
            if (chatState.outboxCount > 0) {
                BadgedBox(badge = { Badge { Text("${chatState.outboxCount}") } }) {
                    TextButton(onClick = { showOutboxSheet = true }) {
                        Text("Outbox")
                    }
                }
            }
        }

        chatState.error?.let { msg ->
            Spacer(Modifier.height(8.dp))
            AssistChip(
                onClick = onClearError,
                label = { Text(text = msg) }
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            reverseLayout = true
        ) {
            items(chatState.messages.reversed(), key = { it.id }) { m ->
                val prefix = if (m.direction == ChatMessageUi.Direction.IN) "Them: " else "Me: "
                Text(
                    text = "$prefix${m.text}",
                    modifier = Modifier.animateItemPlacement()
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = destination,
            onValueChange = onDestinationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = "Destination") }
        )

        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                label = { Text(text = "Message") }
            )
            Button(onClick = onSend) {
                Text(text = "Send")
            }
        }
    }

    if (showOutboxSheet) {
        ModalBottomSheet(onDismissRequest = { showOutboxSheet = false }) {
            OutboxSheetContent(
                state = outboxState,
                onRetryAll = onRetryOutbox,
                onClearAll = onClearOutbox
            )
        }
    }
}
