package com.example.breakingsilence.screens

import android.Manifest
import android.content.Context
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.breakingsilence.R
import com.example.breakingsilence.detection.HandGestureDetector
import com.example.breakingsilence.detection.MLKitHandOverlay
import com.example.breakingsilence.ui.theme.Turquoise
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // State for detected hand pose
    var detectedPose by remember { mutableStateOf("") }
    
    // State for detected pose object
    var currentPose by remember { mutableStateOf<Pose?>(null) }
    
    // State for image dimensions
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    
    // State for camera selector (front/back)
    var cameraSelector by remember { mutableStateOf(CameraSelector.DEFAULT_BACK_CAMERA) }
    
    // Camera permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Hand gesture detector
    val handGestureDetector = remember { HandGestureDetector() }
    
    // Clean up resources when the composable is disposed
    DisposableEffect(handGestureDetector) {
        onDispose {
            handGestureDetector.close()
        }
    }
    
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
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Turquoise)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SCAN",
                    color = Color.Black,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (cameraPermissionState.status.isGranted) {
                    // Camera is available, show camera preview
                    Box(modifier = Modifier.fillMaxSize()) {
                        CameraPreview(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            cameraSelector = cameraSelector,
                            handGestureDetector = handGestureDetector,
                            onPoseDetected = { pose, width, height ->
                                currentPose = pose
                                imageWidth = width
                                imageHeight = height
                                detectedPose = if (pose != null && HandGestureDetector.hasHandLandmarks(pose)) {
                                    HandGestureDetector.identifyHandGesture(pose)
                                } else {
                                    "No hand detected"
                                }
                            }
                        )
                        
                        // Draw the hand pose overlay
                        if (currentPose != null) {
                            MLKitHandOverlay(
                                pose = currentPose,
                                imageWidth = imageWidth,
                                imageHeight = imageHeight,
                                detectedGesture = detectedPose
                            )
                        }
                        
                        // Overlay the viewfinder on top of the camera preview
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Camera viewfinder
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_viewfinder),
                                    contentDescription = "Camera viewfinder",
                                    modifier = Modifier.size(250.dp)
                                )
                            }
                            
                            // Camera switch button
                            IconButton(
                                onClick = {
                                    cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .size(48.dp)
                                    .background(Turquoise.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_switch_camera),
                                    contentDescription = "Switch camera",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Request camera permission
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                            "Camera access is needed to detect hand gestures. Please grant the permission."
                        } else {
                            "Camera permission is required for this feature. Please grant the permission."
                        }
                        
                        Text(textToShow)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { cameraPermissionState.launchPermissionRequest() },
                            colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                        ) {
                            Text("Request Permission", color = Color.Black)
                        }
                    }
                }
            }
            
            // Bottom section with detected hand and back button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Hand icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Turquoise, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hand),
                        contentDescription = "Hand icon",
                        modifier = Modifier.size(60.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Back button
                Button(
                    onClick = onBackClick,
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Turquoise)
                ) {
                    Text(
                        text = "BACK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraSelector: CameraSelector,
    handGestureDetector: HandGestureDetector,
    onPoseDetected: (Pose?, Int, Int) -> Unit
) {
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Camera preview
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
            previewView
        },
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                // Image analysis use case
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(640, 480)) // Set a reasonable resolution for hand detection
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, PoseAnalyzer(handGestureDetector, onPoseDetected))
                    }
                
                try {
                    // Unbind all use cases before rebinding
                    cameraProvider.unbindAll()
                    
                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * Image analyzer that uses ML Kit for hand pose detection
 */
class PoseAnalyzer(
    private val handGestureDetector: HandGestureDetector,
    private val onPoseDetected: (Pose?, Int, Int) -> Unit
) : ImageAnalysis.Analyzer {
    
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )
            
            val width = mediaImage.width
            val height = mediaImage.height
            
            // Process the image with ML Kit's pose detector
            handGestureDetector.detectPose(image) { pose, w, h ->
                onPoseDetected(pose, width, height)
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}
