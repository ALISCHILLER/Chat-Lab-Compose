package com.msa.chatlab.feature.chat.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.data.outbox.OutboxItem
import com.msa.chatlab.feature.chat.vm.OutboxUiState

@Composable
fun OutboxSheetContent(
    state: OutboxUiState,
    onRetryAll: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Outbox", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRetryAll, enabled = state.failedItems.isNotEmpty()) { Text("Retry All") }
                Button(onClick = onClearAll, enabled = state.failedItems.isNotEmpty()) { Text("Clear Failed") }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.pendingItems.isEmpty() && state.inFlightItems.isEmpty() && state.failedItems.isEmpty()) {
            Text("Outbox is empty.")
        }

        if (state.inFlightItems.isNotEmpty()) {
            Text("In-Flight (${state.inFlightItems.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                items(state.inFlightItems) { item -> OutboxRow(item) }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (state.pendingItems.isNotEmpty()) {
            Text("Pending (${state.pendingItems.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                items(state.pendingItems) { item -> OutboxRow(item) }
            }
        }

        if (state.failedItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Failed (${state.failedItems.size})", style = MaterialTheme.typography.titleMedium)
            LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                items(state.failedItems) { item -> OutboxRow(item) }
            }
        }
    }
}

@Composable
private fun OutboxRow(item: OutboxItem) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("ID: ${item.messageId}", style = MaterialTheme.typography.bodySmall)
        Text("Status: ${item.status}", style = MaterialTheme.typography.bodySmall)
        Text("Attempt: ${item.attempt}, LastAttemptAt: ${item.lastAttemptAt ?: "-"}", style = MaterialTheme.typography.bodySmall)
        Text("Error: ${item.lastError ?: "-"}", style = MaterialTheme.typography.bodySmall)
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
