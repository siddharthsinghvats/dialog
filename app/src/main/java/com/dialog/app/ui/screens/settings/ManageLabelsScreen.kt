package com.dialog.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.MeasurementLabel
import com.dialog.app.data.repository.DiaLogRepository
import kotlinx.coroutines.launch

/**
 * Screen for managing measurement labels (viewing and adding custom labels).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLabelsScreen(
    repository: DiaLogRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    val allLabels by repository.getAllLabels().collectAsState(initial = emptyList())
    val customLabels by repository.getCustomLabels().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var newLabelName by remember { mutableStateOf("") }
    var newLabelNameHi by remember { mutableStateOf("") }
    var labelToDelete by remember { mutableStateOf<MeasurementLabel?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Manage Labels",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Label")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Default labels section
            item {
                Text(
                    text = "Default Labels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "These labels come pre-installed and cannot be deleted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(allLabels.filter { !it.isCustom }) { label ->
                LabelItem(
                    label = label,
                    canDelete = false,
                    onDeleteClick = {}
                )
            }
            
            // Custom labels section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Custom Labels",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Labels you've created",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (customLabels.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = "No custom labels yet. Tap + to add one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(customLabels, key = { it.id }) { label ->
                    LabelItem(
                        label = label,
                        canDelete = true,
                        onDeleteClick = { labelToDelete = label }
                    )
                }
            }
            
            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    // Add label dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                newLabelName = ""
                newLabelNameHi = ""
            },
            title = { Text("Add Custom Label") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newLabelName,
                        onValueChange = { newLabelName = it },
                        label = { Text("Label Name (English) *") },
                        placeholder = { Text("e.g., Before Exercise") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = newLabelNameHi,
                        onValueChange = { newLabelNameHi = it },
                        label = { Text("Label Name (Hindi) - Optional") },
                        placeholder = { Text("e.g., व्यायाम से पहले") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.createCustomLabel(
                                nameEn = newLabelName.trim(),
                                nameHi = newLabelNameHi.trim().ifBlank { null }
                            )
                            showAddDialog = false
                            newLabelName = ""
                            newLabelNameHi = ""
                        }
                    },
                    enabled = newLabelName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showAddDialog = false
                    newLabelName = ""
                    newLabelNameHi = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    labelToDelete?.let { label ->
        AlertDialog(
            onDismissRequest = { labelToDelete = null },
            title = { Text("Delete Label?") },
            text = { Text("Delete \"${label.nameEn}\"? Records using this label will keep their data.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteCustomLabel(label.id)
                            labelToDelete = null
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
                TextButton(onClick = { labelToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Individual label item in the list.
 */
@Composable
private fun LabelItem(
    label: MeasurementLabel,
    canDelete: Boolean,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label.nameEn,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (!label.nameHi.isNullOrBlank()) {
                    Text(
                        text = label.nameHi!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (canDelete) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
