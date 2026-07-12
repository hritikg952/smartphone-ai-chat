package com.smartphoneaichat.presentation.state

import android.graphics.Bitmap

data class ScannerUiState(
    val cameraPermissionGranted: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val isAnalyzing: Boolean = false,
)