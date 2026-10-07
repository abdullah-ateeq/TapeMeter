package com.example.tapemeter.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    companion object {
        val bottomNavItems = listOf(
            NavItem(
                route = Screen.Calculator.route,
                title = "Calculator",
                selectedIcon = Icons.Filled.Calculate,
                unselectedIcon = Icons.Outlined.Calculate
            ),
            NavItem(
                route = Screen.History.route,
                title = "History",
                selectedIcon = Icons.Filled.History,
                unselectedIcon = Icons.Outlined.History
            ),
            NavItem(
                route = Screen.Settings.route,
                title = "Settings",
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            ),
            NavItem(
                route = Screen.About.route,
                title = "About",
                selectedIcon = Icons.Filled.Info,
                unselectedIcon = Icons.Outlined.Info
            )
        )
    }
}
