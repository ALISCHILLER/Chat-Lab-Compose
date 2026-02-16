package com.msa.chatlab.feature.chat.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.feature.chat.state.ChatUiState

@Composable
fun ChatScreen(
    state: ChatUiState,
    input: String,
    destination: String,
    onInputChange: (String) -> Unit,
    onDestinationChange: (String) -> Unit,
    onSend: () -> Unit,
    onClearError: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text(text = "Profile: ${state.profileName}")
        Spacer(Modifier.height(8.dp))
        Text(text = "Outbox: ${state.outboxCount}")

        state.error?.let { msg ->
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
            items(state.messages.reversed()) { m ->
                Text(text = "${m.from}: ${m.text}")
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
}
