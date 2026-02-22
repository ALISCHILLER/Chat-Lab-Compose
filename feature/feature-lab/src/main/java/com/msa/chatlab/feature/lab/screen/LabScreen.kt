package com.msa.chatlab.feature.lab.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.lab.RunProgress
import com.msa.chatlab.core.domain.model.RunResult
import com.msa.chatlab.core.domain.model.Scenario
import com.msa.chatlab.feature.lab.state.LabUiEvent
import com.msa.chatlab.feature.lab.state.LabUiState

@Composable
fun LabScreen(
    state: LabUiState,
    onEvent: (LabUiEvent) -> Unit,
    padding: PaddingValues
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Run", "Results")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> RunTab(state = state, onEvent = onEvent)
            1 -> ResultsScreen(results = state.pastResults, padding = PaddingValues())
        }
    }

    // ✅ فاز ۱.۶: Stop Confirmation Dialog
    if (state.showStopConfirm) {
        AlertDialog(
            onDismissRequest = { onEvent(LabUiEvent.DismissStopConfirm) },
            title = { Text("Stop scenario?") },
            text = { Text("Are you sure you want to stop the running scenario?") },
            confirmButton = {
                Button(onClick = { onEvent(LabUiEvent.ConfirmStop) }) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(LabUiEvent.DismissStopConfirm) }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RunTab(state: LabUiState, onEvent: (LabUiEvent) -> Unit) {
    val busy = state.progress.status == RunProgress.Status.Running || state.progress.status == RunProgress.Status.Stopping

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Lab", style = MaterialTheme.typography.headlineSmall)

        ScenarioPresetsSection(onEvent = onEvent, isRunning = busy)

        if (busy && state.activeScenario != null) {
            RunningSection(
                scenario = state.activeScenario,
                progress = state.progress,
                onStop = { onEvent(LabUiEvent.StopPressed) }
            )
        }

        // ✅ فاز ۱.۶: Summary Card
        if (state.runResult != null && state.activeScenario != null) {
            RunSummaryCard(
                scenario = state.activeScenario,
                result = state.runResult,
                canRetry = !busy && state.lastPreset != null,
                onCopy = { onEvent(LabUiEvent.CopyLastRunSummary) },
                onRetry = { onEvent(LabUiEvent.RetryLast) }
            )
        }

        state.errorMessage?.let {
            ErrorSection(message = it, onClear = { onEvent(LabUiEvent.ClearResults) })
        }
    }
}

@Composable
private fun ScenarioPresetsSection(onEvent: (LabUiEvent) -> Unit, isRunning: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Scenario Presets", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(LabUiEvent.StartStable) }, enabled = !isRunning) { Text("Stable") }
            Button(onClick = { onEvent(LabUiEvent.StartIntermittent) }, enabled = !isRunning) { Text("Intermittent") }
            Button(onClick = { onEvent(LabUiEvent.StartOfflineBurst) }, enabled = !isRunning) { Text("Offline Burst") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onEvent(LabUiEvent.StartLossy) }, enabled = !isRunning) { Text("Lossy") }
            Button(onClick = { onEvent(LabUiEvent.StartLoadBurst) }, enabled = !isRunning) { Text("Load Burst") }
        }
    }
}

@Composable
private fun RunningSection(
    scenario: Scenario,
    progress: RunProgress,
    onStop: () -> Unit
) {
    val stopping = progress.status == RunProgress.Status.Stopping

    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (stopping) "Stopping: ${scenario.name}" else "Running: ${scenario.name}",
                style = MaterialTheme.typography.titleMedium
            )

            LinearProgressIndicator(
                progress = { (progress.percent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Progress: ${progress.percent}% (${progress.elapsedMs / 1000}s)")

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CounterCard(title = "Sent", value = progress.sentCount.toString())
                CounterCard(title = "Received", value = progress.successCount.toString())
                CounterCard(title = "Failed", value = progress.failCount.toString())
            }

            Button(
                onClick = onStop,
                enabled = !stopping,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(if (stopping) "Stopping…" else "Stop")
            }
        }
    }
}

@Composable
private fun CounterCard(title: String, value: String) {
    Card {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun RunSummaryCard(
    scenario: Scenario,
    result: RunResult,
    canRetry: Boolean,
    onCopy: () -> Unit,
    onRetry: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Run Summary", style = MaterialTheme.typography.titleMedium)

            Text("Scenario: ${scenario.name}  •  Pattern: ${scenario.pattern}")
            Text("Duration: ${scenario.durationSec}s  •  RPS: ${scenario.rps}  •  Payload: ${scenario.payloadBytes} bytes")

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(onClick = {}, label = { Text("Enq: ${result.enqueuedCount}") })
                AssistChip(onClick = {}, label = { Text("Sent: ${result.sentCount}") })
                AssistChip(onClick = {}, label = { Text("Recv: ${result.receivedCount}") })
                AssistChip(onClick = {}, label = { Text("Fail: ${result.failedCount}") })
            }

            Text("Success: " + "%.2f".format(result.successRatePercent) + "%   •   TPS: " + "%.2f".format(result.throughputMsgPerSec))
            Text("Latency avg/p50/p95/p99: ${result.latencyAvgMs ?: "-"} / ${result.latencyP50Ms ?: "-"} / ${result.latencyP95Ms ?: "-"} / ${result.latencyP99Ms ?: "-"} ms")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCopy) { Text("Copy metrics") }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onRetry, enabled = canRetry) { Text("Retry last") }
            }
        }
    }
}

@Composable
private fun ErrorSection(message: String, onClear: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Error", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
            Text(message)
            Button(onClick = onClear, modifier = Modifier.align(Alignment.End)) {
                Text("Clear")
            }
        }
    }
}
