package com.dialog.app.ui.screens.record

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.GlucoseRecord
import com.dialog.app.data.model.MeasurementLabel
import com.dialog.app.data.model.Profile
import com.dialog.app.data.repository.DiaLogRepository
import com.dialog.app.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen for adding or editing a glucose record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecordScreen(
    repository: DiaLogRepository,
    recordId: Long?,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditing = recordId != null
    
    // Get selected profile
    val selectedProfile by repository.getSelectedProfile().collectAsState(initial = null)
    val labels by repository.getAllLabels().collectAsState(initial = emptyList())
    
    // Form state
    var glucoseLevel by remember { mutableStateOf("") }
    var selectedLabelId by remember { mutableStateOf<Long?>(null) }
    var notes by remember { mutableStateOf("") }
    var measuredAt by remember { mutableStateOf(System.currentTimeMillis()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Load existing record if editing
    LaunchedEffect(recordId) {
        recordId?.let { id ->
            repository.getRecordById(id)?.let { recordWithLabel ->
                glucoseLevel = recordWithLabel.record.glucoseLevel.toString()
                selectedLabelId = recordWithLabel.record.labelId
                notes = recordWithLabel.record.notes ?: ""
                measuredAt = recordWithLabel.record.measuredAt
            }
        }
    }
    
    // Auto-select first label if none selected
    LaunchedEffect(labels) {
        if (selectedLabelId == null && labels.isNotEmpty()) {
            selectedLabelId = labels.first().id
        }
    }
    
    // Validation
    val glucoseValue = glucoseLevel.toIntOrNull()
    val isValid = glucoseValue != null && glucoseValue > 0 && selectedProfile != null
    
    // Calculate risk level for preview
    val riskLevel = if (glucoseValue != null && selectedProfile != null) {
        selectedProfile!!.getRiskLevel(glucoseValue)
    } else null
    
    // Date/time formatters
    val dateFormat = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Reading" else "Add Reading",
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
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
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
        if (selectedProfile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    title = "No profile selected",
                    subtitle = "Please select a profile first"
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Risk level preview (if glucose entered)
                if (glucoseValue != null && riskLevel != null) {
                    GlucoseDisplayCard(
                        glucoseLevel = glucoseValue,
                        riskLevel = riskLevel,
                        labelName = labels.find { it.id == selectedLabelId }?.nameEn
                    )
                }
                
                // Glucose level input
                Text(
                    text = "Glucose Level (mg/dL) *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = glucoseLevel,
                    onValueChange = { 
                        if (it.all { c -> c.isDigit() } && it.length <= 4) {
                            glucoseLevel = it
                        }
                    },
                    placeholder = { Text("Enter glucose level") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Measurement label selection
                Text(
                    text = "Measurement Time",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                LabelChipRow(
                    labels = labels,
                    selectedLabelId = selectedLabelId,
                    onLabelSelected = { selectedLabelId = it.id },
                    modifier = Modifier.fillMaxWidth() // Removed negative padding
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Date and Time selection
                Text(
                    text = "When was this reading taken?",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                // Quick time buttons
                QuickTimeSelector(
                    onTimeSelected = { measuredAt = it }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date picker button
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = dateFormat.format(Date(measuredAt)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Time picker button
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = timeFormat.format(Date(measuredAt)),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Notes
                Text(
                    text = "Notes (optional)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = { Text("Add any notes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Save button
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            
                            val record = GlucoseRecord(
                                id = recordId ?: 0,
                                profileId = selectedProfile!!.id,
                                glucoseLevel = glucoseValue!!,
                                labelId = selectedLabelId,
                                notes = notes.ifBlank { null },
                                measuredAt = measuredAt
                            )
                            
                            if (isEditing) {
                                repository.updateRecord(record)
                            } else {
                                repository.addRecord(record)
                            }
                            
                            isLoading = false
                            onNavigateBack()
                        }
                    },
                    enabled = isValid && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = if (isEditing) "Save Changes" else "Save Reading",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = measuredAt
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            // Preserve the time, update only the date
                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = measuredAt
                            
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.timeInMillis = selectedDate
                            
                            calendar.set(Calendar.YEAR, selectedCalendar.get(Calendar.YEAR))
                            calendar.set(Calendar.MONTH, selectedCalendar.get(Calendar.MONTH))
                            calendar.set(Calendar.DAY_OF_MONTH, selectedCalendar.get(Calendar.DAY_OF_MONTH))
                            
                            measuredAt = calendar.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = measuredAt
        
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )
        
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        calendar.set(Calendar.MINUTE, timePickerState.minute)
                        measuredAt = calendar.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Reading?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            recordId?.let { repository.deleteRecord(it) }
                            showDeleteDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
