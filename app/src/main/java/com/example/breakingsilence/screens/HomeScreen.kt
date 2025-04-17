package com.example.breakingsilence.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.breakingsilence.R
import com.example.breakingsilence.ui.theme.Turquoise

@Composable
fun HomeScreen(
    onTutorialsClick: () -> Unit,
    onTranslateClick: () -> Unit,
    onScanClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background pattern
        Image(
            painter = painterResource(id = R.drawable.background_pattern),
            contentDescription = "Background pattern",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Turquoise)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BREAKING SILENCE",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Hand gesture logo
            Image(
                painter = painterResource(id = R.drawable.ic_hand_gesture),
                contentDescription = "Hand gesture logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Buttons
            AppButton(text = "TUTORIALS", onClick = onTutorialsClick)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppButton(text = "TRANSLATE", onClick = onTranslateClick)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppButton(text = "SCAN", onClick = onScanClick)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AppButton(text = "SETTINGS", onClick = onSettingsClick)
        }
    }
}

@Composable
fun AppButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
