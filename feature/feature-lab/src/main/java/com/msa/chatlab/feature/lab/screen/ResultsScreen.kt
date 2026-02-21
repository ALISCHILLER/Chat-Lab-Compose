package com.msa.chatlab.feature.lab.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.domain.model.RunResult

@Composable
fun ResultsScreen(results: List<RunResult>, padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
    ) {
        Text("Past Results", style = MaterialTheme.typography.headlineSmall)

        if (results.isEmpty()) {
            Text("No results yet. Run a scenario to see its results here.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    LatencyChart(
                        results = results,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }
                items(results) { result ->
                    ResultCard(result = result)
                }
            }
        }
    }
}

@Composable
private fun ResultCard(result: RunResult) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(result.session.scenario.name, style = MaterialTheme.typography.titleMedium)
            Text("Sent: ${result.sentCount} • Received: ${result.receivedCount} • Failed: ${result.failedCount}")
            Text("Success: ${"%.2f".format(result.successRatePercent)}% • Throughput: ${"%.2f".format(result.throughputMsgPerSec)} msg/s")
            Text("Latency p95: ${result.latencyP95Ms?.let { "$it ms" } ?: "-"}")
        }
    }
}

@Composable
private fun LatencyChart(results: List<RunResult>, modifier: Modifier = Modifier) {
    val values = results.map { it.latencyP95Ms ?: 0L }
    val max = (values.maxOrNull() ?: 0L).toFloat().let { if (it > 0f) it else 1f }
    val strokeColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val path = Path()
        values.forEachIndexed { index, v ->
            val denom = (values.size - 1).coerceAtLeast(1)
            val x = size.width * (index.toFloat() / denom)
            val ratio = (v.toFloat() / max).coerceIn(0f, 1f)
            val y = size.height * (1f - ratio)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, strokeColor, style = Stroke(width = 3f))
    }
}