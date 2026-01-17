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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.GlucoseRecordWithLabel
import com.dialog.app.ui.theme.GlucoseHigh
import com.dialog.app.ui.theme.GlucoseLow
import com.dialog.app.ui.theme.GlucoseNormal
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High quality trend graph showing glucose levels over time.
 */
@Composable
fun GlucoseTrendGraph(
    records: List<GlucoseRecordWithLabel>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) return

    // Prepare data for chart
    // Vico expects entries sorted by x-axis
    val sortedRecords = remember(records) {
        records.sortedBy { it.record.measuredAt }
    }

    val chartEntryModel = remember(sortedRecords) {
        val entries = sortedRecords.mapIndexed { index, record ->
            FloatEntry(
                x = index.toFloat(), // Use index for X-axis to keep equal spacing
                y = record.record.glucoseLevel.toFloat()
            )
        }
        entryModelOf(entries)
    }

    // Date formatter for X-axis
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
    val fillColorStart = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val fillColorEnd = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Glucose Trend",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
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
                    .height(200.dp)
            )
        }
    }
}
