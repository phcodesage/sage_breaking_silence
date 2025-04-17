package com.example.breakingsilence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.breakingsilence.navigation.AppNavigation
import com.example.breakingsilence.ui.theme.BreakingSilenceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreakingSilenceTheme {
                BreakingSilenceApp()
            }
        }
    }
}

@Composable
fun BreakingSilenceApp() {
    val navController = rememberNavController()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        AppNavigation(navController = navController)
    }
}