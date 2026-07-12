package com.smartphoneaichat.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartphoneaichat.presentation.state.CaptureStatus
import com.smartphoneaichat.presentation.viewmodel.ScannerViewModel
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AccentRed
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.DarkSurfaceVariant
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onImageCaptured: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (permissionGranted) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        modifier = modifier,
    ) { innerPadding ->
        if (!state.cameraPermissionGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Camera access required",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
            val preview = remember { Preview.Builder().build() }
            val imageCapture = remember {
                ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .aspectRatio(4f / 5f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp)),
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageCapture,
                                    )
                                    preview.setSurfaceProvider(previewView.surfaceProvider)
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            cameraProviderFuture.addListener({
                                cameraProviderFuture.get().unbindAll()
                            }, ContextCompat.getMainExecutor(context))
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = {
                            viewModel.setAnalyzing(true)
                            imageCapture.takePicture(
                                cameraExecutor,
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(image: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(image)
                                        image.close()
                                        viewModel.onBitmapCaptured(bitmap)
                                        viewModel.saveCapturedBitmap { path ->
                                            onImageCaptured(path)
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        viewModel.setAnalyzing(false)
                                    }
                                },
                            )
                        },
                        enabled = !state.isAnalyzing && state.captureStatus != CaptureStatus.Saving,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth(0.5f),
                    ) {
                        Text(
                            text = if (state.isAnalyzing) "Analyzing..." else "Analyze",
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    Text(
                        text = when {
                            state.captureStatus == CaptureStatus.Saving -> "Saving image..."
                            state.captureStatus == CaptureStatus.Saved -> "Captured!"
                            state.captureStatus == CaptureStatus.Error -> state.captureError ?: "Capture failed"
                            state.isAnalyzing -> "Capturing..."
                            state.capturedBitmap != null -> "Image captured"
                            else -> "Point camera at text"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.captureStatus == CaptureStatus.Error) AccentRed else TextSecondary,
                    )
                }
            }
        }
    }
}

private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
    val buffer = image.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    val matrix = Matrix().apply { postRotate(image.imageInfo.rotationDegrees.toFloat()) }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}