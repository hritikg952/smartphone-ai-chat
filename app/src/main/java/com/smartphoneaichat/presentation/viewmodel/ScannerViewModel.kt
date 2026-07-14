package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartphoneaichat.presentation.state.CaptureStatus
import com.smartphoneaichat.presentation.state.ScannerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    fun saveCapturedBitmap(onSaved: (String) -> Unit) {
        val bitmap = _state.value.capturedBitmap ?: return
        _state.update { it.copy(captureStatus = CaptureStatus.Saving, captureError = null) }

        viewModelScope.launch {
            try {
                val path = withContext(Dispatchers.IO) {
                    val capturesDir = File(getApplication<Application>().filesDir, "captures")
                    if (!capturesDir.exists()) capturesDir.mkdirs()

                    cleanupOldCaptures(capturesDir)

                    val filename = "capture_${System.currentTimeMillis()}.jpg"
                    val file = File(capturesDir, filename)
                    FileOutputStream(file).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                    }
                    file.absolutePath
                }
                _state.update {
                    it.copy(
                        capturedImagePath = path,
                        captureStatus = CaptureStatus.Saved,
                    )
                }
                if (isActive) {
                    onSaved(path)
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        captureStatus = CaptureStatus.Error,
                        captureError = e.message ?: "Failed to save image",
                    )
                }
            }
        }
    }

    fun resetCapture() {
        _state.update {
            it.copy(
                capturedBitmap = null,
                capturedImagePath = null,
                captureStatus = CaptureStatus.Idle,
                captureError = null,
            )
        }
    }

    fun setCaptureError(message: String) {
        _state.update {
            it.copy(
                captureStatus = CaptureStatus.Error,
                captureError = message,
            )
        }
    }

    private fun cleanupOldCaptures(dir: File) {
        val cutoff = System.currentTimeMillis() - 3600_000
        dir.listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _state.update { it.copy(capturedBitmap = null) }
    }
}