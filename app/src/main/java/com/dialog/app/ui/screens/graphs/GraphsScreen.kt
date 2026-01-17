package com.dialog.app.ui.screens.graphs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dialog.app.data.repository.DiaLogRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphsScreen(
    repository: DiaLogRepository,
    onNavigateBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Weekly", "Monthly")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glucose Trends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> {
                    // Weekly graph
                    Text(text = "Weekly graph will be displayed here.")
                }
                1 -> {
                    // Monthly graph
                    Text(text = "Monthly graph will be displayed here.")
                }
            }
        }
    }
}