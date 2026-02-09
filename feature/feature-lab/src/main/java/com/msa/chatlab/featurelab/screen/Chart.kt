package com.msa.chatlab.featurelab.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.msa.chatlab.core.data.lab.RunResult

@Composable
fun LatencyChart(
    results: List<RunResult>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
            }
        },
        update = { chart ->
            val entries = results.mapIndexed { index, result ->
                Entry(index.toFloat(), result.latencyP95Ms?.toFloat() ?: 0f)
            }
            val dataSet = LineDataSet(entries, "P95 Latency")
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
