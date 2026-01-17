package com.dialog.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.dialog.app.data.repository.DiaLogRepository
import com.dialog.app.ui.screens.analytics.AnalyticsScreen
import com.dialog.app.ui.screens.dashboard.DashboardScreen
import com.dialog.app.ui.screens.history.HistoryScreen
import com.dialog.app.ui.screens.profile.AddEditProfileScreen
import com.dialog.app.ui.screens.profile.ProfileListScreen
import com.dialog.app.ui.screens.record.AddEditRecordScreen
import com.dialog.app.ui.screens.settings.ManageLabelsScreen

/**
 * Main navigation graph for the app.
 * Handles all screen transitions with animations.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    repository: DiaLogRepository,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                repository = repository,
                onNavigateToAddRecord = { navController.navigate(Screen.AddRecord.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfiles = { navController.navigate(Screen.Profiles.route) }
            )
        }
        
        // Add Record
        composable(Screen.AddRecord.route) {
            AddEditRecordScreen(
                repository = repository,
                recordId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Analytics
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                repository = repository
            )
        }
        
        // Edit Record
        composable(
            route = Screen.EditRecord.route,
            arguments = listOf(navArgument("recordId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong("recordId")
            AddEditRecordScreen(
                repository = repository,
                recordId = recordId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // History
        composable(Screen.History.route) {
            HistoryScreen(
                repository = repository,
                onNavigateToEditRecord = { recordId ->
                    navController.navigate(Screen.EditRecord.createRoute(recordId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Profiles List
        composable(Screen.Profiles.route) {
            ProfileListScreen(
                repository = repository,
                onNavigateToAddProfile = { navController.navigate(Screen.AddProfile.route) },
                onNavigateToEditProfile = { profileId ->
                    navController.navigate(Screen.EditProfile.createRoute(profileId))
                },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Add Profile
        composable(Screen.AddProfile.route) {
            AddEditProfileScreen(
                repository = repository,
                profileId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Edit Profile
        composable(
            route = Screen.EditProfile.route,
            arguments = listOf(navArgument("profileId") { type = NavType.LongType })
        ) { backStackEntry ->
            val profileId = backStackEntry.arguments?.getLong("profileId")
            AddEditProfileScreen(
                repository = repository,
                profileId = profileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        // Manage Labels
        composable(Screen.ManageLabels.route) {
            ManageLabelsScreen(
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
