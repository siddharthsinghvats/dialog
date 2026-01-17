package com.dialog.app.ui.screens.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.RiskLevel
import com.dialog.app.data.repository.DiaLogRepository
import com.dialog.app.ui.components.EmptyState
import com.dialog.app.ui.components.FilterChip
import com.dialog.app.ui.components.GlucoseRecordCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * History screen showing all glucose records with filtering options.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    repository: DiaLogRepository,
    onNavigateToEditRecord: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Get selected profile
    val selectedProfile by repository.getSelectedProfile().collectAsState(initial = null)
    val labels by repository.getAllLabels().collectAsState(initial = emptyList())
    
    // Records for selected profile
    val allRecords by selectedProfile?.let { profile ->
        repository.getRecordsForProfile(profile.id).collectAsState(initial = emptyList())
    } ?: remember { mutableStateOf(emptyList()) }
    
    // Filter state
    var showFilters by remember { mutableStateOf(false) }
    var selectedLabelFilter by remember { mutableStateOf<Long?>(null) }
    var selectedRiskFilter by remember { mutableStateOf<RiskLevel?>(null) }
    var selectedDateFilter by remember { mutableStateOf<DateFilter>(DateFilter.ALL) }
    
    // Filtered records
    val filteredRecords by remember(allRecords, selectedLabelFilter, selectedRiskFilter, selectedDateFilter, selectedProfile) {
        derivedStateOf {
            var records = allRecords
            
            // Filter by label
            if (selectedLabelFilter != null) {
                records = records.filter { it.record.labelId == selectedLabelFilter }
            }
            
            // Filter by risk level
            if (selectedRiskFilter != null && selectedProfile != null) {
                records = records.filter { 
                    selectedProfile!!.getRiskLevel(it.record.glucoseLevel) == selectedRiskFilter 
                }
            }
            
            // Filter by date
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()
            
            when (selectedDateFilter) {
                DateFilter.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    val startOfDay = calendar.timeInMillis
                    records = records.filter { it.record.measuredAt >= startOfDay }
                }
                DateFilter.WEEK -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    records = records.filter { it.record.measuredAt >= calendar.timeInMillis }
                }
                DateFilter.MONTH -> {
                    calendar.add(Calendar.MONTH, -1)
                    records = records.filter { it.record.measuredAt >= calendar.timeInMillis }
                }
                DateFilter.ALL -> { /* No filter */ }
            }
            
            records
        }
    }
    
    // Date formatters
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    // Clear all filters
    fun clearFilters() {
        selectedLabelFilter = null
        selectedRiskFilter = null
        selectedDateFilter = DateFilter.ALL
    }
    
    val hasActiveFilters = selectedLabelFilter != null || 
                           selectedRiskFilter != null || 
                           selectedDateFilter != DateFilter.ALL
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "History",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (hasActiveFilters) {
                                Badge { }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilters = !showFilters }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter"
                            )
                        }
                    }
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
        ) {
            // Filter section
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date filters
                        Text(
                            text = "Date Range",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(DateFilter.entries.toTypedArray()) { filter ->
                                FilterChip(
                                    label = filter.displayName,
                                    isSelected = selectedDateFilter == filter,
                                    onClick = { selectedDateFilter = filter }
                                )
                            }
                        }
                        
                        // Risk level filters
                        Text(
                            text = "Risk Level",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    label = "All",
                                    isSelected = selectedRiskFilter == null,
                                    onClick = { selectedRiskFilter = null }
                                )
                            }
                            items(RiskLevel.entries.toTypedArray()) { risk ->
                                FilterChip(
                                    label = risk.displayName,
                                    isSelected = selectedRiskFilter == risk,
                                    onClick = { selectedRiskFilter = risk }
                                )
                            }
                        }
                        
                        // Label filters
                        if (labels.isNotEmpty()) {
                            Text(
                                text = "Measurement Type",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                item {
                                    FilterChip(
                                        label = "All",
                                        isSelected = selectedLabelFilter == null,
                                        onClick = { selectedLabelFilter = null }
                                    )
                                }
                                items(labels) { label ->
                                    FilterChip(
                                        label = label.nameEn,
                                        isSelected = selectedLabelFilter == label.id,
                                        onClick = { selectedLabelFilter = label.id }
                                    )
                                }
                            }
                        }
                        
                        // Clear filters button
                        if (hasActiveFilters) {
                            TextButton(
                                onClick = { clearFilters() },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Clear Filters")
                            }
                        }
                    }
                }
            }
            
            // Results count
            if (filteredRecords.isNotEmpty()) {
                Text(
                    text = "${filteredRecords.size} reading${if (filteredRecords.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Records list
            if (selectedProfile == null) {
                EmptyState(
                    title = "No profile selected",
                    subtitle = "Please select a profile to view history",
                    modifier = Modifier.weight(1f)
                )
            } else if (filteredRecords.isEmpty()) {
                EmptyState(
                    title = if (hasActiveFilters) "No matching readings" else "No readings yet",
                    subtitle = if (hasActiveFilters) "Try adjusting your filters" else "Add your first glucose reading to see it here",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredRecords,
                        key = { it.record.id }
                    ) { recordWithLabel ->
                        val riskLevel = selectedProfile!!.getRiskLevel(recordWithLabel.record.glucoseLevel)
                        
                        GlucoseRecordCard(
                            glucoseLevel = recordWithLabel.record.glucoseLevel,
                            riskLevel = riskLevel,
                            labelName = recordWithLabel.label?.nameEn,
                            time = timeFormat.format(Date(recordWithLabel.record.measuredAt)),
                            date = dateFormat.format(Date(recordWithLabel.record.measuredAt)),
                            notes = recordWithLabel.record.notes,
                            onClick = { onNavigateToEditRecord(recordWithLabel.record.id) },
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Date filter options.
 */
enum class DateFilter(val displayName: String) {
    ALL("All Time"),
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month")
}
