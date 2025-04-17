package com.example.breakingsilence.detection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * A composable that draws an overlay for visualizing hand landmarks using ML Kit
 */
@Composable
fun MLKitHandOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    detectedGesture: String,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (pose == null) return@Canvas
        
        // Scale factors to convert from image coordinates to canvas coordinates
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight
        
        // Draw the gesture text
        val textLayoutResult = textMeasurer.measure(
            text = detectedGesture,
            style = TextStyle(
                fontSize = 24.sp,
                color = Color.Green
            )
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(size.width / 2 - textLayoutResult.size.width / 2, 50f)
        )
        
        // Draw hand landmarks
        for (landmarkType in HandGestureDetector.HAND_LANDMARK_TYPES) {
            val landmark = pose.getPoseLandmark(landmarkType) ?: continue
            
            val position = Offset(
                landmark.position.x * scaleX,
                landmark.position.y * scaleY
            )
            
            // Draw landmark point
            drawCircle(
                color = Color.Red,
                radius = 8f,
                center = position
            )
        }
        
        // Draw connections between landmarks
        for ((startLandmarkType, endLandmarkType) in HandGestureDetector.HAND_CONNECTIONS) {
            val startLandmark = pose.getPoseLandmark(startLandmarkType)
            val endLandmark = pose.getPoseLandmark(endLandmarkType)
            
            if (startLandmark != null && endLandmark != null) {
                val startPosition = Offset(
                    startLandmark.position.x * scaleX,
                    startLandmark.position.y * scaleY
                )
                
                val endPosition = Offset(
                    endLandmark.position.x * scaleX,
                    endLandmark.position.y * scaleY
                )
                
                drawLine(
                    color = Color.Green,
                    start = startPosition,
                    end = endPosition,
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
