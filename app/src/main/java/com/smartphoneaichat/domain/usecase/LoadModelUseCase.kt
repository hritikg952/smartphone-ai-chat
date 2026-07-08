package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.modelInfoById
import com.smartphoneaichat.domain.repository.ModelFileManager

class LoadModelUseCase(
    private val modelFileManager: ModelFileManager,
) {
    suspend operator fun invoke(modelId: String, onProgress: (Float) -> Unit = {}): Result<Unit> {
        val modelInfo = modelInfoById(modelId)
            ?: return Result.failure(IllegalArgumentException("Unknown model: $modelId"))
        modelFileManager.unloadModel()
        return modelFileManager.loadModel(modelInfo, onProgress)
    }
}