package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import com.smartphoneaichat.presentation.state.ScannerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _state.update { it.copy(cameraPermissionGranted = granted) }
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        _state.update { it.copy(capturedBitmap = bitmap, isAnalyzing = false) }
    }

    fun setAnalyzing(analyzing: Boolean) {
        _state.update { it.copy(isAnalyzing = analyzing) }
    }

    fun clearCapturedBitmap() {
        _state.update { it.copy(capturedBitmap = null) }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(capturedBitmap = null) }
    }
}