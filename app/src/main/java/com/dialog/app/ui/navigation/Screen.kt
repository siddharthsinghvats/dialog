package com.dialog.app.ui.navigation

/**
 * Navigation routes for the app.
 * Using sealed class for type-safe navigation.
 */
sealed class Screen(val route: String) {
    // Main screens (with bottom nav)
    object Dashboard : Screen("dashboard")
    object AddRecord : Screen("add_record")
    object History : Screen("history")
    object Profiles : Screen("profiles")
    object Graphs : Screen("graphs")
    
    // Secondary screens (without bottom nav)
    object AddProfile : Screen("add_profile")
    object EditProfile : Screen("edit_profile/{profileId}") {
        fun createRoute(profileId: Long) = "edit_profile/$profileId"
    }
    object EditRecord : Screen("edit_record/{recordId}") {
        fun createRoute(recordId: Long) = "edit_record/$recordId"
    }
    object ManageLabels : Screen("manage_labels")
    object Settings : Screen("settings")
    
    // Onboarding
    object Onboarding : Screen("onboarding")
}

/**
 * Bottom navigation items configuration.
 */
enum class BottomNavItem(
    val screen: Screen,
    val labelEn: String,
    val labelHi: String,
    val iconName: String
) {
    DASHBOARD(Screen.Dashboard, "Dashboard", "डैशबोर्ड", "dashboard"),
    ADD(Screen.AddRecord, "Add", "जोड़ें", "add_circle"),
    GRAPHS(Screen.Graphs, "Graphs", "ग्राफ", "show_chart"),
    HISTORY(Screen.History, "Records", "रिकॉर्ड्स", "history"), // Previously "History" / "इतिहास"
    PROFILES(Screen.Profiles, "Profile", "प्रोफाइल", "person")
}
