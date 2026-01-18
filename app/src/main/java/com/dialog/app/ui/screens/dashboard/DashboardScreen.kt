package com.dialog.app.ui.screens.dashboard

import com.dialog.app.ui.components.GlucoseTrendGraph

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.Profile
import com.dialog.app.data.model.GlucoseRecordWithLabel
import com.dialog.app.data.repository.DiaLogRepository
import com.dialog.app.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main dashboard screen showing the latest glucose reading and quick stats.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: DiaLogRepository,
    onNavigateToAddRecord: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfiles: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    // Observe selected profile
    val selectedProfile by repository.getSelectedProfile().collectAsState(initial = null)
    
    // Observe latest record for selected profile
    val latestRecord by selectedProfile?.let { profile ->
        repository.getLatestRecord(profile.id).collectAsState(initial = null)
    } ?: remember { mutableStateOf(null) }
    
    // Observe recent records (last 10)
    val recentRecords by selectedProfile?.let { profile ->
        repository.getRecentRecords(profile.id, limit = 10).collectAsState(initial = emptyList())
    } ?: remember { mutableStateOf(emptyList()) }
    
    // Observe today's record count
    val todayCount by selectedProfile?.let { profile ->
        repository.getTodayRecordCount(profile.id).collectAsState(initial = 0)
    } ?: remember { mutableStateOf(0) }
    
    // Calculate averages
    var todayAverage by remember { mutableStateOf<Double?>(null) }
    var weekAverage by remember { mutableStateOf<Double?>(null) }
    
    LaunchedEffect(selectedProfile) {
        selectedProfile?.let { profile ->
            todayAverage = repository.getTodayAverageGlucose(profile.id)
            weekAverage = repository.getWeekAverageGlucose(profile.id)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SugarTrack",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Profile selector
                    ProfileSelector(
                        profile = selectedProfile,
                        onClick = onNavigateToProfiles
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddRecord,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = { Text("Add Reading") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (selectedProfile == null) {
                // No profile selected
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👋 Welcome to SugarTrack!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a profile to start tracking your glucose levels",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToProfiles,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Create Profile")
                        }
                    }
                }
            } else {
                // Trend Graph (only if we have records)
                if (recentRecords.isNotEmpty()) {
                    GlucoseTrendGraph(
                        records = recentRecords.reversed() // Reverse to show oldest to newest left-to-right
                    )
                }
                
                // Recent Records List
                if (recentRecords.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Recent Records",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        recentRecords.forEach { recordWithLabel ->
                           val riskLevel = selectedProfile!!.getRiskLevel(recordWithLabel.record.glucoseLevel)
                           val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                           val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                           
                           GlucoseRecordCard(
                               glucoseLevel = recordWithLabel.record.glucoseLevel,
                               riskLevel = riskLevel,
                               labelName = null, // Hidden as requested by user
                               time = timeFormat.format(Date(recordWithLabel.record.measuredAt)),
                               date = dateFormat.format(Date(recordWithLabel.record.measuredAt)),
                               notes = recordWithLabel.record.notes,
                               onClick = { /* History allows editing, dashboard is read-only view for now */ }
                           )
                        }
                        
                        // Center aligned "View All" button
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextButton(onClick = onNavigateToHistory) {
                                Text("View All Records")
                            }
                        }
                    }
                }
            }
            
            // Bottom spacer for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
