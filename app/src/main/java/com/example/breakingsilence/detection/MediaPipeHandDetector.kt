package com.example.breakingsilence.detection

import android.content.Context
import android.graphics.Bitmap
import android.util.Log

/**
 * A stub class for MediaPipeHandDetector - we're now using ML Kit instead
 * This is kept to maintain backward compatibility with existing code
 */
class MediaPipeHandDetector(
    private val context: Context,
    private val listener: HandDetectionListener
) {
    /**
     * Interface for hand detection callbacks
     */
    interface HandDetectionListener {
        fun onHandDetected(result: Any?, width: Int, height: Int)
        fun onHandDetectionError(error: String)
    }
    
    /**
     * Detect hands in the provided bitmap - stub method
     */
    fun detectHands(bitmap: Bitmap, rotationDegrees: Int) {
        // This is a stub method - we're using ML Kit now
        Log.i(TAG, "MediaPipe hand detection is deprecated, using ML Kit instead")
    }
    
    /**
     * Close the detector and release resources - stub method
     */
    fun close() {
        // Nothing to close
    }
    
    companion object {
        private const val TAG = "MediaPipeHandDetector"
    }
}
