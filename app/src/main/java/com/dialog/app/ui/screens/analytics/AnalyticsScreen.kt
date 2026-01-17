package com.dialog.app.ui.screens.analytics

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.GlucoseRecordWithLabel
import com.dialog.app.data.model.MeasurementLabel
import com.dialog.app.data.repository.DiaLogRepository
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollState
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

/**
 * Time period options for analytics view
 */
enum class TimePeriod(val label: String, val days: Int) {
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30)
}

/**
 * Comprehensive analytics screen with interactive charts, 
 * stats cards, label filters, and time period selector.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    repository: DiaLogRepository
) {
    val scope = rememberCoroutineScope()
    
    // State
    var selectedTimePeriod by remember { mutableStateOf(TimePeriod.WEEKLY) }
    var selectedLabelId by remember { mutableStateOf<Long?>(null) }
    
    // Observe selected profile
    val selectedProfile by repository.getSelectedProfile().collectAsState(initial = null)
    
    // Observe labels
    val allLabels by repository.getAllLabels().collectAsState(initial = emptyList())
    
    // Calculate time range
    val now = System.currentTimeMillis()
    val startTime = now - (selectedTimePeriod.days * 24 * 60 * 60 * 1000L)
    
    // Observe records for the time period
    val records by selectedProfile?.let { profile ->
        if (selectedLabelId != null) {
            repository.getRecordsByLabel(profile.id, selectedLabelId!!)
        } else {
            repository.getRecordsByDateRange(profile.id, startTime, now)
        }
    }?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    
    // Filter records by time period if we're filtering by label
    val filteredRecords = remember(records, startTime, selectedLabelId) {
        if (selectedLabelId != null) {
            records.filter { it.record.measuredAt >= startTime }
        } else {
            records
        }
    }
    
    // Calculate stats
    var averageGlucose by remember { mutableStateOf<Double?>(null) }
    var maxGlucose by remember { mutableStateOf<Int?>(null) }
    var minGlucose by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(selectedProfile, selectedTimePeriod, filteredRecords) {
        selectedProfile?.let { profile ->
            averageGlucose = repository.getAverageGlucose(profile.id, startTime, now)
            maxGlucose = repository.getMaxGlucose(profile.id, startTime, now)
            minGlucose = repository.getMinGlucose(profile.id, startTime, now)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analytics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (selectedProfile == null) {
                // No profile message
                EmptyAnalyticsCard(
                    message = "Please create a profile to view analytics"
                )
            } else if (filteredRecords.isEmpty()) {
                // Time Period Selector (still show even when no data)
                TimePeriodSelector(
                    selectedPeriod = selectedTimePeriod,
                    onPeriodSelected = { selectedTimePeriod = it }
                )
                
                EmptyAnalyticsCard(
                    message = "No readings found for the selected period"
                )
            } else {
                // Time Period Selector
                TimePeriodSelector(
                    selectedPeriod = selectedTimePeriod,
                    onPeriodSelected = { selectedTimePeriod = it }
                )
                
                // Stats Cards
                StatsRow(
                    average = averageGlucose,
                    max = maxGlucose,
                    min = minGlucose
                )
                
                // Label Filter Chips
                LabelFilterSection(
                    labels = allLabels,
                    selectedLabelId = selectedLabelId,
                    onLabelSelected = { labelId ->
                        selectedLabelId = if (selectedLabelId == labelId) null else labelId
                    }
                )
                
                // Interactive Chart
                AnalyticsChart(
                    records = filteredRecords,
                    timePeriod = selectedTimePeriod
                )
                
                // Summary Card 
                SummaryCard(
                    recordCount = filteredRecords.size,
                    timePeriod = selectedTimePeriod
                )
                
                // Bottom spacer
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TimePeriodSelector(
    selectedPeriod: TimePeriod,
    onPeriodSelected: (TimePeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimePeriod.entries.forEach { period ->
                val isSelected = period == selectedPeriod
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onPeriodSelected(period) },
                    color = if (isSelected) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = period.label,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    average: Double?,
    max: Int?,
    min: Int?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Average",
            value = average?.let { "%.0f".format(it) } ?: "--",
            unit = "mg/dL",
            icon = Icons.Default.TrendingFlat,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Maximum",
            value = max?.toString() ?: "--",
            unit = "mg/dL",
            icon = Icons.Default.ArrowUpward,
            iconColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Minimum",
            value = min?.toString() ?: "--",
            unit = "mg/dL",
            icon = Icons.Default.ArrowDownward,
            iconColor = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LabelFilterSection(
    labels: List<MeasurementLabel>,
    selectedLabelId: Long?,
    onLabelSelected: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Filter by Label",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedLabelId == null,
                onClick = { 
                    if (selectedLabelId != null) {
                        onLabelSelected(selectedLabelId!!)
                    }
                },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
            
            labels.forEach { label ->
                FilterChip(
                    selected = selectedLabelId == label.id,
                    onClick = { onLabelSelected(label.id) },
                    label = { Text(label.nameEn) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun AnalyticsChart(
    records: List<GlucoseRecordWithLabel>,
    timePeriod: TimePeriod
) {
    if (records.isEmpty()) return
    
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
    
    // Date format based on time period
    val dateFormat = remember(timePeriod) {
        if (timePeriod == TimePeriod.WEEKLY) {
            SimpleDateFormat("EEE", Locale.getDefault())
        } else {
            SimpleDateFormat("dd/MM", Locale.getDefault())
        }
    }
    
    val horizontalAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index >= 0 && index < sortedRecords.size) {
            val date = Date(sortedRecords[index].record.measuredAt)
            dateFormat.format(date)
        } else {
            ""
        }
    }
    
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColorStart = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
    val fillColorEnd = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
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
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${sortedRecords.size} readings",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val chartScrollState = rememberChartScrollState()
            
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
                chartScrollState = chartScrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }
    }
}

@Composable
private fun SummaryCard(
    recordCount: Int,
    timePeriod: TimePeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Readings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$recordCount",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Time Period",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last ${timePeriod.days} days",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EmptyAnalyticsCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
