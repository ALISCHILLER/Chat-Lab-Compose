package com.msa.chatlab.feature.lab.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.data.lab.Scenario
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
}

@Composable
private fun RunTab(state: LabUiState, onEvent: (LabUiEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Lab", style = MaterialTheme.typography.headlineSmall)

        // سناریوهای پیش‌فرض
        ScenarioPresetsSection(onEvent = onEvent, isRunning = state.isRunning)

        // پیشرفت اجرا
        if (state.isRunning && state.activeScenario != null) {
            RunningSection(
                scenario = state.activeScenario,
                progressPercent = state.progressPercent,
                counters = state.counters,
                onStop = { onEvent(LabUiEvent.Stop) }
            )
        }

        // نتایج
        state.runResult?.let { result ->
            ResultsSection(result = result, onClear = { onEvent(LabUiEvent.ClearResults) })
        }

        // خطا
        state.errorMessage?.let { error ->
            ErrorSection(message = error, onClear = { onEvent(LabUiEvent.ClearResults) })
        }
    }
}

@Composable
private fun ScenarioPresetsSection(onEvent: (LabUiEvent) -> Unit, isRunning: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Scenario Presets", style = MaterialTheme.typography.titleMedium)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onEvent(LabUiEvent.StartStable) },
                enabled = !isRunning
            ) { Text("Stable") }

            Button(
                onClick = { onEvent(LabUiEvent.StartIntermittent) },
                enabled = !isRunning
            ) { Text("Intermittent") }

            Button(
                onClick = { onEvent(LabUiEvent.StartOfflineBurst) },
                enabled = !isRunning
            ) { Text("Offline Burst") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onEvent(LabUiEvent.StartLossy) },
                enabled = !isRunning
            ) { Text("Lossy") }

            Button(
                onClick = { onEvent(LabUiEvent.StartLoadBurst) },
                enabled = !isRunning
            ) { Text("Load Burst") }
        }
    }
}

@Composable
private fun RunningSection(
    scenario: Scenario,
    progressPercent: Int,
    counters: LabUiState.Counters,
    onStop: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Running: ${scenario.preset.name}", style = MaterialTheme.typography.titleMedium)

            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Progress: $progressPercent%")

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CounterCard(title = "Sent", value = counters.sent.toString())
                CounterCard(title = "Received", value = counters.received.toString())
                CounterCard(title = "Failed", value = counters.failed.toString())
            }

            Button(onClick = onStop, modifier = Modifier.align(Alignment.End)) {
                Text("Stop")
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
private fun ResultsSection(result: com.msa.chatlab.core.data.lab.RunResult, onClear: () -> Unit) {
    Card {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Results", style = MaterialTheme.typography.titleMedium)

            Text("Sent: ${result.sent}")
            Text("Received: ${result.received}")
            Text("Failed: ${result.failed}")
            Text("Success Rate: ${result.successRatePercent?.let { "%.1f%%".format(it) } ?: "N/A"}")
            Text("Latency P95: ${result.latencyP95Ms?.let { "${it}ms" } ?: "N/A"}")

            Button(onClick = onClear, modifier = Modifier.align(Alignment.End)) {
                Text("Clear Results")
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
