package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.ModelInfo

/**
 * Manages model file lifecycle: download, storage, deletion, loading.
 *
 * Business rationale: Model file management is a distinct concern from
 * inference. Separating them follows ISP — a client that only sends
 * messages should not depend on download or file deletion methods.
 */
interface ModelFileManager {
    suspend fun downloadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit = {}): Result<Unit>
    fun cancelDownload()
    fun isDownloaded(modelInfo: ModelInfo): Boolean
    fun listDownloadedModelIds(): List<String>
    fun deleteModelFile(modelInfo: ModelInfo): Boolean
    suspend fun loadModel(modelInfo: ModelInfo, onProgress: (Float) -> Unit = {}): Result<Unit>
    fun unloadModel()
}