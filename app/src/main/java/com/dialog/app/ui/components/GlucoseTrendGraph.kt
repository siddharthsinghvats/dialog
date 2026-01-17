package com.dialog.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.GlucoseRecordWithLabel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Compact mini-graph for dashboard showing glucose trend at a glance.
 * Simplified styling for quick overview.
 */
@Composable
fun GlucoseTrendGraph(
    records: List<GlucoseRecordWithLabel>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    // Prepare data for chart - sorted by measurement time
    val sortedRecords = remember(records) {
        records.sortedBy { it.record.measuredAt }
    }

    val chartEntryModel = remember(sortedRecords) {
        val entries = sortedRecords.mapIndexed { index, record ->
            FloatEntry(
                x = index.toFloat(),
                y = record.record.glucoseLevel.toFloat()
            )
        }
        entryModelOf(entries)
    }

    // Simple date formatter for X-axis
    val horizontalAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index >= 0 && index < sortedRecords.size) {
            val date = Date(sortedRecords[index].record.measuredAt)
            val format = SimpleDateFormat("dd/MM", Locale.getDefault())
            format.format(date)
        } else {
            ""
        }
    }

    // Colors
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColorStart = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    val fillColorEnd = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Glucose Trend",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Chart(
                chart = lineChart(
                    lines = listOf(
                        LineChart.LineSpec(
                            lineColor = lineColor.toArgb(),
                            lineBackgroundShader = DynamicShaders.fromBrush(
                                Brush.verticalGradient(
                                    listOf(fillColorStart, fillColorEnd)
                                )
                            )
                        )
                    )
                ),
                model = chartEntryModel,
                startAxis = rememberStartAxis(),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = horizontalAxisValueFormatter,
                    guideline = null
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}
