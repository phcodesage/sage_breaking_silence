package com.example.breakingsilence.detection

import android.graphics.PointF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

/**
 * A composable that draws an overlay for visualizing detected hand pose landmarks
 */
@Composable
fun HandPoseOverlay(
    pose: Pose?,
    imageWidth: Int,
    imageHeight: Int,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    
    Canvas(modifier = modifier.fillMaxSize()) {
        if (pose == null) return@Canvas
        
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) return@Canvas
        
        // Scale factors to convert from image coordinates to canvas coordinates
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight
        
        // Detect the gesture
        val gesture = if (HandGestureDetector.hasHandLandmarks(pose)) {
            HandGestureDetector.identifyHandGesture(pose)
        } else {
            "No hand gesture detected"
        }
        
        // Draw the gesture text
        val textLayoutResult = textMeasurer.measure(
            text = gesture,
            style = TextStyle(
                fontSize = 24.sp,
                color = Color.Green
            )
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(size.width / 2 - textLayoutResult.size.width / 2, 100f)
        )
        
        // Draw all landmarks
        landmarks.forEach { landmark ->
            // Only draw hand landmarks
            if (isHandLandmark(landmark.landmarkType)) {
                val position = landmark.position
                val scaledPosition = Offset(
                    position.x * scaleX,
                    position.y * scaleY
                )
                
                // Draw landmark point
                drawCircle(
                    color = Color.Red,
                    radius = 8f,
                    center = scaledPosition
                )
            }
        }
        
        // Draw connections between landmarks to form a skeleton
        drawHandConnections(landmarks, scaleX, scaleY)
    }
}

/**
 * Extension function to draw connections between landmarks
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHandConnections(
    landmarks: List<PoseLandmark>,
    scaleX: Float,
    scaleY: Float
) {
    // Draw each connection
    HandGestureDetector.HAND_CONNECTIONS.forEach { (startLandmarkType, endLandmarkType) ->
        val startLandmark = landmarks.find { it.landmarkType == startLandmarkType }
        val endLandmark = landmarks.find { it.landmarkType == endLandmarkType }
        
        if (startLandmark != null && endLandmark != null) {
            val start = Offset(
                startLandmark.position.x * scaleX,
                startLandmark.position.y * scaleY
            )
            val end = Offset(
                endLandmark.position.x * scaleX,
                endLandmark.position.y * scaleY
            )
            
            drawLine(
                color = Color.Green,
                start = start,
                end = end,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Check if a landmark is a hand landmark
 */
private fun isHandLandmark(landmarkType: Int): Boolean {
    return landmarkType == PoseLandmark.LEFT_WRIST ||
           landmarkType == PoseLandmark.LEFT_THUMB ||
           landmarkType == PoseLandmark.LEFT_INDEX ||
           landmarkType == PoseLandmark.LEFT_PINKY ||
           landmarkType == PoseLandmark.RIGHT_WRIST ||
           landmarkType == PoseLandmark.RIGHT_THUMB ||
           landmarkType == PoseLandmark.RIGHT_INDEX ||
           landmarkType == PoseLandmark.RIGHT_PINKY
}

/**
 * Data class to hold hand pose information
 */
data class HandPoseData(
    val landmarks: List<PointF>,
    val boundingBox: android.graphics.RectF
)
