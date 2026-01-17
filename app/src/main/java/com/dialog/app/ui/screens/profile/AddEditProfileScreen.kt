package com.dialog.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dialog.app.data.model.DiabetesType
import com.dialog.app.data.model.Profile
import com.dialog.app.data.repository.DiaLogRepository
import com.dialog.app.ui.components.ColorPicker
import com.dialog.app.ui.components.ProfileAvatar
import com.dialog.app.ui.theme.AvatarColors
import kotlinx.coroutines.launch

/**
 * Screen for adding or editing a profile.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProfileScreen(
    repository: DiaLogRepository,
    profileId: Long?,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditing = profileId != null
    
    // Form state
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var diabetesType by remember { mutableStateOf(DiabetesType.TYPE_2) }
    var avatarColor by remember { mutableStateOf(AvatarColors.first()) }
    var targetMin by remember { mutableStateOf("70") }
    var targetMax by remember { mutableStateOf("140") }
    var borderlineMax by remember { mutableStateOf("180") }
    
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    
    // Load existing profile if editing
    LaunchedEffect(profileId) {
        profileId?.let { id ->
            repository.getProfileById(id)?.let { profile ->
                name = profile.name
                age = profile.age?.toString() ?: ""
                diabetesType = profile.diabetesType
                avatarColor = try {
                    Color(android.graphics.Color.parseColor(profile.avatarColor))
                } catch (e: Exception) {
                    AvatarColors.first()
                }
                targetMin = profile.targetGlucoseMin.toString()
                targetMax = profile.targetGlucoseMax.toString()
                borderlineMax = profile.borderlineMax.toString()
            }
        }
    }
    
    // Validation
    val isValid = name.isNotBlank() && 
                  (targetMin.toIntOrNull() ?: 0) > 0 &&
                  (targetMax.toIntOrNull() ?: 0) > (targetMin.toIntOrNull() ?: 0) &&
                  (borderlineMax.toIntOrNull() ?: 0) > (targetMax.toIntOrNull() ?: 0)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Profile" else "Add Profile",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar preview
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ProfileAvatar(
                    name = name.ifBlank { "?" },
                    color = avatarColor,
                    size = 96
                )
            }
            
            // Color picker
            Text(
                text = "Avatar Color",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
            ColorPicker(
                colors = AvatarColors,
                selectedColor = avatarColor,
                onColorSelected = { avatarColor = it }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                placeholder = { Text("Enter name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // Age field
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all { c -> c.isDigit() }) age = it },
                label = { Text("Age (optional)") },
                placeholder = { Text("Enter age") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            // Diabetes type dropdown
            ExposedDropdownMenuBox(
                expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it }
            ) {
                OutlinedTextField(
                    value = diabetesType.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Diabetes Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
                    DiabetesType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                diabetesType = type
                                expandedDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Target glucose section
            Text(
                text = "Target Glucose Range (mg/dL)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Customize the glucose levels that are considered normal, borderline, or high for this profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = targetMin,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetMin = it },
                    label = { Text("Min Normal") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = targetMax,
                    onValueChange = { if (it.all { c -> c.isDigit() }) targetMax = it },
                    label = { Text("Max Normal") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            
            OutlinedTextField(
                value = borderlineMax,
                onValueChange = { if (it.all { c -> c.isDigit() }) borderlineMax = it },
                label = { Text("Borderline Max (above this is High)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            
            // Risk level preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Risk Level Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    val minVal = targetMin.toIntOrNull() ?: 70
                    val maxVal = targetMax.toIntOrNull() ?: 140
                    val borderVal = borderlineMax.toIntOrNull() ?: 180
                    Text("🔴 Low: < $minVal mg/dL", style = MaterialTheme.typography.bodySmall)
                    Text("🟢 Normal: $minVal - $maxVal mg/dL", style = MaterialTheme.typography.bodySmall)
                    Text("🟡 Borderline: ${maxVal + 1} - $borderVal mg/dL", style = MaterialTheme.typography.bodySmall)
                    Text("🔴 High: > $borderVal mg/dL", style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Save button
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        val colorHex = String.format("#%06X", 0xFFFFFF and avatarColor.hashCode())
                        
                        val profile = Profile(
                            id = profileId ?: 0,
                            name = name.trim(),
                            age = age.toIntOrNull(),
                            diabetesType = diabetesType,
                            avatarColor = colorHex,
                            targetGlucoseMin = targetMin.toIntOrNull() ?: 70,
                            targetGlucoseMax = targetMax.toIntOrNull() ?: 140,
                            borderlineMax = borderlineMax.toIntOrNull() ?: 180
                        )
                        
                        if (isEditing) {
                            repository.updateProfile(profile)
                        } else {
                            repository.createProfile(profile)
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
                        text = if (isEditing) "Save Changes" else "Create Profile",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile?") },
            text = { Text("This will permanently delete this profile and all their glucose records.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            profileId?.let { repository.deleteProfile(it) }
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
