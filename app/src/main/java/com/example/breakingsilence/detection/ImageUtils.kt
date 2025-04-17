package com.example.breakingsilence.detection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import java.io.ByteArrayOutputStream

/**
 * Utility class for image processing
 */
object ImageUtils {
    
    /**
     * Convert a camera image to a bitmap for processing
     */
    fun imageToBitmap(image: Image, rotationDegrees: Int): Bitmap {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        // Rotate the bitmap if needed
        return if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }
    
    /**
     * Calculate the rotation needed based on device orientation and camera lens facing
     */
    fun getImageRotation(deviceRotation: Int, cameraFacing: Int): Int {
        return when (cameraFacing) {
            // For front camera
            1 -> {
                when (deviceRotation) {
                    0 -> 270 // Portrait
                    90 -> 180 // Landscape right
                    180 -> 90 // Portrait upside down
                    270 -> 0 // Landscape left
                    else -> 0
                }
            }
            // For back camera
            else -> {
                when (deviceRotation) {
                    0 -> 90 // Portrait
                    90 -> 0 // Landscape right
                    180 -> 270 // Portrait upside down
                    270 -> 180 // Landscape left
                    else -> 0
                }
            }
        }
    }
}
