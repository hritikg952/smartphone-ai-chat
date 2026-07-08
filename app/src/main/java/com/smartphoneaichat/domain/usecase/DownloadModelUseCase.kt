package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.modelInfoById
import com.smartphoneaichat.domain.repository.ModelFileManager

class DownloadModelUseCase(
    private val modelFileManager: ModelFileManager,
) {
    suspend operator fun invoke(modelId: String, onProgress: (Float) -> Unit = {}): Result<Unit> {
        val modelInfo = modelInfoById(modelId)
            ?: return Result.failure(IllegalArgumentException("Unknown model: $modelId"))
        return modelFileManager.downloadModel(modelInfo, onProgress)
    }
}