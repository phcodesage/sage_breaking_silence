package com.example.breakingsilence.detection

import android.graphics.PointF
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Enhanced hand gesture detector using ML Kit's pose detection
 */
class HandGestureDetector {
    private val poseDetector: PoseDetector
    
    init {
        // Create pose detector with enhanced settings
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)
            .setPreferredHardwareConfigs(AccuratePoseDetectorOptions.CPU_GPU)
            .build()
        
        poseDetector = PoseDetection.getClient(options)
    }
    
    fun detectPose(image: InputImage, onPoseDetected: (Pose?, Int, Int) -> Unit) {
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                // Check if any pose was detected
                if (pose.allPoseLandmarks.isNotEmpty()) {
                    // Pass the detected pose and image dimensions
                    onPoseDetected(pose, image.width, image.height)
                } else {
                    onPoseDetected(null, image.width, image.height)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Pose detection failed", e)
                onPoseDetected(null, image.width, image.height)
            }
    }
    
    fun close() {
        poseDetector.close()
    }
    
    companion object {
        private const val TAG = "HandGestureDetector"
        
        // Define hand landmark types for drawing
        val HAND_LANDMARK_TYPES = listOf(
            PoseLandmark.LEFT_WRIST,
            PoseLandmark.LEFT_THUMB,
            PoseLandmark.LEFT_INDEX,
            PoseLandmark.LEFT_PINKY,
            PoseLandmark.RIGHT_WRIST,
            PoseLandmark.RIGHT_THUMB,
            PoseLandmark.RIGHT_INDEX,
            PoseLandmark.RIGHT_PINKY
        )
        
        // Define connections between landmarks for drawing
        val HAND_CONNECTIONS = listOf(
            // Wrist to thumb
            PoseLandmark.LEFT_WRIST to PoseLandmark.LEFT_THUMB,
            PoseLandmark.RIGHT_WRIST to PoseLandmark.RIGHT_THUMB,
            
            // Wrist to pinky
            PoseLandmark.LEFT_WRIST to PoseLandmark.LEFT_PINKY,
            PoseLandmark.RIGHT_WRIST to PoseLandmark.RIGHT_PINKY,
            
            // Wrist to index
            PoseLandmark.LEFT_WRIST to PoseLandmark.LEFT_INDEX,
            PoseLandmark.RIGHT_WRIST to PoseLandmark.RIGHT_INDEX,
            
            // Hand connections
            PoseLandmark.LEFT_PINKY to PoseLandmark.LEFT_INDEX,
            PoseLandmark.RIGHT_PINKY to PoseLandmark.RIGHT_INDEX,
            
            // Thumb to index
            PoseLandmark.LEFT_THUMB to PoseLandmark.LEFT_INDEX,
            PoseLandmark.RIGHT_THUMB to PoseLandmark.RIGHT_INDEX
        )
        
        /**
         * Check if a pose contains hand landmarks
         */
        fun hasHandLandmarks(pose: Pose): Boolean {
            val handLandmarks = listOf(
                PoseLandmark.LEFT_WRIST,
                PoseLandmark.LEFT_THUMB,
                PoseLandmark.LEFT_INDEX,
                PoseLandmark.LEFT_PINKY,
                PoseLandmark.RIGHT_WRIST,
                PoseLandmark.RIGHT_THUMB,
                PoseLandmark.RIGHT_INDEX,
                PoseLandmark.RIGHT_PINKY
            )
            
            return handLandmarks.any { landmark ->
                pose.getPoseLandmark(landmark) != null
            }
        }
        
        /**
         * Identify the hand gesture from the pose
         */
        fun identifyHandGesture(pose: Pose): String {
            // Check if we have enough landmarks to identify gestures
            if (!hasHandLandmarks(pose)) return "No hand detected"
            
            // First check for sign language letters and numbers
            val signLanguageGesture = identifySignLanguage(pose)
            if (signLanguageGesture.isNotEmpty()) {
                return signLanguageGesture
            }
            
            // If no sign language gesture is detected, check for basic gestures
            return when {
                isPointingGesture(pose) -> "Pointing"
                isOpenPalmGesture(pose) -> "Open Palm"
                isClosedFistGesture(pose) -> "Closed Fist"
                isPeaceSignGesture(pose) -> "Peace Sign"
                isThumbsUpGesture(pose) -> "Thumbs Up"
                else -> "Unknown Gesture"
            }
        }
        
        /**
         * Identify sign language letters and numbers
         */
        private fun identifySignLanguage(pose: Pose): String {
            // Try to identify which hand is more visible/complete
            val leftHandComplete = isLeftHandComplete(pose)
            val rightHandComplete = isRightHandComplete(pose)
            
            // Prefer the hand that has more landmarks detected
            return if (leftHandComplete && (!rightHandComplete || isLeftHandDominant(pose))) {
                identifyLeftHandSignLanguage(pose)
            } else if (rightHandComplete) {
                identifyRightHandSignLanguage(pose)
            } else {
                ""
            }
        }
        
        /**
         * Check if left hand has enough landmarks for sign language detection
         */
        private fun isLeftHandComplete(pose: Pose): Boolean {
            val requiredLandmarks = listOf(
                PoseLandmark.LEFT_WRIST,
                PoseLandmark.LEFT_THUMB,
                PoseLandmark.LEFT_INDEX,
                PoseLandmark.LEFT_PINKY
            )
            
            return requiredLandmarks.all { pose.getPoseLandmark(it) != null }
        }
        
        /**
         * Check if right hand has enough landmarks for sign language detection
         */
        private fun isRightHandComplete(pose: Pose): Boolean {
            val requiredLandmarks = listOf(
                PoseLandmark.RIGHT_WRIST,
                PoseLandmark.RIGHT_THUMB,
                PoseLandmark.RIGHT_INDEX,
                PoseLandmark.RIGHT_PINKY
            )
            
            return requiredLandmarks.all { pose.getPoseLandmark(it) != null }
        }
        
        /**
         * Determine if left hand is more dominant in the frame
         */
        private fun isLeftHandDominant(pose: Pose): Boolean {
            val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            
            // If one wrist is missing, the other is dominant
            if (leftWrist == null) return false
            if (rightWrist == null) return true
            
            // The hand closer to the center of the frame is considered dominant
            // Assuming x=0 is left edge, higher x values are to the right
            return leftWrist.position.x > rightWrist.position.x
        }
        
        /**
         * Identify sign language using left hand landmarks
         */
        private fun identifyLeftHandSignLanguage(pose: Pose): String {
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: return ""
            val thumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?: return ""
            val index = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: return ""
            val pinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?: return ""
            
            // Normalize positions relative to wrist
            val thumbRel = normalizePosition(thumb.position, wrist.position)
            val indexRel = normalizePosition(index.position, wrist.position)
            val pinkyRel = normalizePosition(pinky.position, wrist.position)
            
            // Calculate angles and distances for gesture recognition
            val thumbIndexAngle = calculateAngle(wrist.position, thumb.position, index.position)
            val thumbPinkyAngle = calculateAngle(wrist.position, thumb.position, pinky.position)
            val indexPinkyAngle = calculateAngle(wrist.position, index.position, pinky.position)
            
            val thumbWristDist = distance(thumb.position, wrist.position)
            val indexWristDist = distance(index.position, wrist.position)
            val pinkyWristDist = distance(pinky.position, wrist.position)
            
            // Identify letters
            return when {
                // A - Fist with thumb to the side
                isClosedFistGesture(pose) && thumbRel.x > 0.1f -> "Sign: Letter A"
                
                // B - Fingers extended upward, thumb across palm
                indexWristDist > 0.15f && pinkyWristDist > 0.15f && 
                thumbRel.y > 0 && indexRel.y < -0.1f -> "Sign: Letter B"
                
                // C - Curved hand like holding a C
                thumbIndexAngle in 30f..90f && thumbPinkyAngle > 120f && 
                thumbRel.y < 0 && pinkyRel.y < 0 -> "Sign: Letter C"
                
                // 1 - Index finger pointing up, others closed
                isPointingGesture(pose) -> "Sign: Number 1"
                
                // 2 - Index and middle fingers extended (peace sign)
                isPeaceSignGesture(pose) -> "Sign: Number 2"
                
                // 3 - Thumb, index, and middle extended
                thumbRel.y < 0 && indexRel.y < 0 && 
                thumbIndexAngle > 30f && thumbPinkyAngle > 60f -> "Sign: Number 3"
                
                // 5 - All fingers extended (open palm)
                isOpenPalmGesture(pose) -> "Sign: Number 5"
                
                else -> ""
            }
        }
        
        /**
         * Identify sign language using right hand landmarks
         */
        private fun identifyRightHandSignLanguage(pose: Pose): String {
            val wrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST) ?: return ""
            val thumb = pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB) ?: return ""
            val index = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX) ?: return ""
            val pinky = pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY) ?: return ""
            
            // Normalize positions relative to wrist
            val thumbRel = normalizePosition(thumb.position, wrist.position)
            val indexRel = normalizePosition(index.position, wrist.position)
            val pinkyRel = normalizePosition(pinky.position, wrist.position)
            
            // Calculate angles and distances for gesture recognition
            val thumbIndexAngle = calculateAngle(wrist.position, thumb.position, index.position)
            val thumbPinkyAngle = calculateAngle(wrist.position, thumb.position, pinky.position)
            val indexPinkyAngle = calculateAngle(wrist.position, index.position, pinky.position)
            
            val thumbWristDist = distance(thumb.position, wrist.position)
            val indexWristDist = distance(index.position, wrist.position)
            val pinkyWristDist = distance(pinky.position, wrist.position)
            
            // Identify letters - mirror of left hand logic with adjustments for right hand
            return when {
                // A - Fist with thumb to the side
                isClosedFistGesture(pose) && thumbRel.x < -0.1f -> "Sign: Letter A"
                
                // B - Fingers extended upward, thumb across palm
                indexWristDist > 0.15f && pinkyWristDist > 0.15f && 
                thumbRel.y > 0 && indexRel.y < -0.1f -> "Sign: Letter B"
                
                // C - Curved hand like holding a C
                thumbIndexAngle in 30f..90f && thumbPinkyAngle > 120f && 
                thumbRel.y < 0 && pinkyRel.y < 0 -> "Sign: Letter C"
                
                // 1 - Index finger pointing up, others closed
                isPointingGesture(pose) -> "Sign: Number 1"
                
                // 2 - Index and middle fingers extended (peace sign)
                isPeaceSignGesture(pose) -> "Sign: Number 2"
                
                // 3 - Thumb, index, and middle extended
                thumbRel.y < 0 && indexRel.y < 0 && 
                thumbIndexAngle > 30f && thumbPinkyAngle > 60f -> "Sign: Number 3"
                
                // 5 - All fingers extended (open palm)
                isOpenPalmGesture(pose) -> "Sign: Number 5"
                
                else -> ""
            }
        }
        
        /**
         * Normalize a position relative to a reference point
         */
        private fun normalizePosition(position: PointF, reference: PointF): PointF {
            return PointF(
                position.x - reference.x,
                position.y - reference.y
            )
        }
        
        private fun isPointingGesture(pose: Pose): Boolean {
            // Check if index finger is extended while others are curled
            val indexTip = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val pinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            
            if (indexTip != null && wrist != null && pinky != null) {
                // Check if index finger is extended (higher than wrist)
                val indexExtended = indexTip.position.y < wrist.position.y
                // Check if pinky is lower than index (curled)
                val pinkyLower = pinky.position.y > indexTip.position.y
                
                return indexExtended && pinkyLower
            }
            return false
        }
        
        private fun isOpenPalmGesture(pose: Pose): Boolean {
            // Check if all fingers are extended
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val thumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
            val index = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val pinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            
            if (wrist != null && thumb != null && index != null && pinky != null) {
                // Check if all fingers are extended (higher than wrist)
                return thumb.position.y < wrist.position.y &&
                       index.position.y < wrist.position.y &&
                       pinky.position.y < wrist.position.y
            }
            return false
        }
        
        private fun isClosedFistGesture(pose: Pose): Boolean {
            // Check if all fingers are curled
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val thumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
            val index = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val pinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            
            if (wrist != null && thumb != null && index != null && pinky != null) {
                // Check if all fingers are close to the wrist (curled)
                val thumbCurled = distance(thumb.position, wrist.position) < 50
                val indexCurled = distance(index.position, wrist.position) < 50
                val pinkyCurled = distance(pinky.position, wrist.position) < 50
                
                return thumbCurled && indexCurled && pinkyCurled
            }
            return false
        }
        
        private fun isPeaceSignGesture(pose: Pose): Boolean {
            // Check if index and middle fingers are extended while others are curled
            val index = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val pinky = pose.getPoseLandmark(PoseLandmark.LEFT_PINKY) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
            
            if (index != null && wrist != null && pinky != null) {
                // Check if index finger is extended (higher than wrist)
                val indexExtended = index.position.y < wrist.position.y
                // Check if pinky is lower than index (curled)
                val pinkyLower = pinky.position.y > index.position.y
                
                return indexExtended && pinkyLower
            }
            return false
        }
        
        private fun isThumbsUpGesture(pose: Pose): Boolean {
            // Check if thumb is extended while others are curled
            val thumb = pose.getPoseLandmark(PoseLandmark.LEFT_THUMB) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
            val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val index = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX) ?: pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            
            if (thumb != null && wrist != null && index != null) {
                // Check if thumb is extended (to the side of wrist)
                val thumbExtended = thumb.position.y < wrist.position.y
                // Check if index is curled (close to wrist)
                val indexCurled = distance(index.position, wrist.position) < 50
                
                return thumbExtended && indexCurled
            }
            return false
        }
        
        private fun distance(p1: PointF, p2: PointF): Float {
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            return sqrt(dx.pow(2) + dy.pow(2))
        }
        
        /**
         * Calculate the angle between three points in degrees
         */
        private fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Float {
            val angle1 = atan2((p1.y - p2.y).toDouble(), (p1.x - p2.x).toDouble())
            val angle2 = atan2((p3.y - p2.y).toDouble(), (p3.x - p2.x).toDouble())
            var angle = Math.toDegrees(angle1 - angle2).toFloat()
            if (angle < 0) angle += 360f
            return angle
        }
    }
}
