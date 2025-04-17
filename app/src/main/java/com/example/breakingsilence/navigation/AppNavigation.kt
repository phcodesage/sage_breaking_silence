package com.example.breakingsilence.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.breakingsilence.screens.HomeScreen
import com.example.breakingsilence.screens.ScanScreen

object AppDestinations {
    const val HOME_ROUTE = "home"
    const val SCAN_ROUTE = "scan"
    const val TUTORIALS_ROUTE = "tutorials"
    const val TRANSLATE_ROUTE = "translate"
    const val SETTINGS_ROUTE = "settings"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(
                onTutorialsClick = { navController.navigate(AppDestinations.TUTORIALS_ROUTE) },
                onTranslateClick = { navController.navigate(AppDestinations.TRANSLATE_ROUTE) },
                onScanClick = { navController.navigate(AppDestinations.SCAN_ROUTE) },
                onSettingsClick = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }
            )
        }
        
        composable(AppDestinations.SCAN_ROUTE) {
            ScanScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        
        // Other screens will be implemented later
        composable(AppDestinations.TUTORIALS_ROUTE) {
            // Placeholder for tutorials screen
            HomeScreen(
                onTutorialsClick = {},
                onTranslateClick = {},
                onScanClick = {},
                onSettingsClick = {}
            )
        }
        
        composable(AppDestinations.TRANSLATE_ROUTE) {
            // Placeholder for translate screen
            HomeScreen(
                onTutorialsClick = {},
                onTranslateClick = {},
                onScanClick = {},
                onSettingsClick = {}
            )
        }
        
        composable(AppDestinations.SETTINGS_ROUTE) {
            // Placeholder for settings screen
            HomeScreen(
                onTutorialsClick = {},
                onTranslateClick = {},
                onScanClick = {},
                onSettingsClick = {}
            )
        }
    }
}
