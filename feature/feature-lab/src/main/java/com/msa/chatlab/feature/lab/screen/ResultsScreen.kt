package com.msa.chatlab.feature.lab.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.msa.chatlab.core.data.lab.RunResult

@Composable
fun ResultsScreen(
    results: List<RunResult>,
    padding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
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
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Run Summary", style = MaterialTheme.typography.titleMedium)
            Text(text = result.toString())
        }
    }
}

@Composable
private fun LatencyChart(
    results: List<RunResult>,
    modifier: Modifier = Modifier,
) {
    val p95latencies = results.map { it.p95LatencyMillis }
    val maxLatency = p95latencies.maxOrNull()?.toFloat() ?: 0f

    Canvas(modifier = modifier) {
        val path = Path()
        p95latencies.forEachIndexed { index, latency ->
            val x = size.width * (index.toFloat() / (p95latencies.size - 1).coerceAtLeast(1))
            val y = size.height * (1 - (latency.toFloat() / maxLatency).coerceIn(0f, 1f))
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path, Color.Blue, style = Stroke(width = 3f))
    }
}
