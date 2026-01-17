package com.dialog.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dialog.app.ui.navigation.NavGraph
import com.dialog.app.ui.navigation.Screen
import com.dialog.app.ui.theme.DiaLogTheme

/**
 * Main activity for DiaLog.
 * Sets up the navigation and bottom navigation bar.
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get repository from application
        val app = application as DiaLogApp
        val repository = app.repository
        
        setContent {
            DiaLogTheme {
                MainScreen(repository = repository)
            }
        }
    }
}

/**
 * Main screen composable with bottom navigation.
 */
@Composable
fun MainScreen(
    repository: com.dialog.app.data.repository.DiaLogRepository
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Bottom nav items
    val bottomNavItems = listOf(
        BottomNavItemData(
            route = Screen.Dashboard.route,
            label = "Dashboard",
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard
        ),
        BottomNavItemData(
            route = Screen.AddRecord.route,
            label = "Add",
            selectedIcon = Icons.Filled.AddCircle,
            unselectedIcon = Icons.Outlined.AddCircle
        ),
        BottomNavItemData(
            route = Screen.Analytics.route,
            label = "Analytics",
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart
        ),
        BottomNavItemData(
            route = Screen.History.route,
            label = "History",
            selectedIcon = Icons.Filled.History,
            unselectedIcon = Icons.Outlined.History
        ),
        BottomNavItemData(
            route = Screen.Profiles.route,
            label = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )
    
    // Screens that should show bottom navigation
    val showBottomNav = currentDestination?.route in listOf(
        Screen.Dashboard.route,
        Screen.AddRecord.route,
        Screen.Analytics.route,
        Screen.History.route,
        Screen.Profiles.route
    )
    
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { 
                            it.route == item.route 
                        } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination to avoid building up a large stack
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavGraph(
                navController = navController,
                repository = repository
            )
        }
    }
}

/**
 * Data class for bottom navigation items.
 */
data class BottomNavItemData(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
