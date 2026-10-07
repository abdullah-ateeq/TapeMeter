package com.example.tapemeter.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tapemeter.ui.navigation.NavItem
import com.example.tapemeter.ui.navigation.Screen
import com.example.tapemeter.ui.screens.about.AboutScreen
import com.example.tapemeter.ui.screens.calculator.CalculatorScreen
import com.example.tapemeter.ui.screens.history.HistoryScreen
import com.example.tapemeter.ui.screens.settings.SettingsScreen
import com.example.tapemeter.ui.screens.splash.SplashScreen
import com.example.tapemeter.ui.theme.TapeMeterTheme
import com.example.tapemeter.ui.viewmodel.TapeMeterViewModel

@Composable
fun TapeMeterApp(
    viewModel: TapeMeterViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    TapeMeterTheme(
        appTheme = settings.theme,
        dynamicColor = settings.dynamicColor
    ) {
        val showBottomBar = currentRoute != Screen.Splash.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavItem.bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(Screen.Calculator.route) {
                                popUpTo(Screen.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                composable(Screen.Calculator.route) {
                    CalculatorScreen(viewModel = viewModel)
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        viewModel = viewModel,
                        onOpenCalculation = {
                            navController.navigate(Screen.Calculator.route) {
                                popUpTo(Screen.Calculator.route) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }

                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}
