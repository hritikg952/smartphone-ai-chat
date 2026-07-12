package com.smartphoneaichat.presentation.state

import android.graphics.Bitmap

enum class CaptureStatus { Idle, Saving, Saved, Error }

data class ScannerUiState(
    val cameraPermissionGranted: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val capturedImagePath: String? = null,
    val captureStatus: CaptureStatus = CaptureStatus.Idle,
    val captureError: String? = null,
)