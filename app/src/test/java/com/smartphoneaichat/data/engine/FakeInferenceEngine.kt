package com.smartphoneaichat.data.engine

import com.smartphoneaichat.domain.repository.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeInferenceEngine(
    private val tokens: List<String> = listOf("Hello", " ", "World"),
    private val shouldThrow: Boolean = false,
    private val throwAfterTokens: Int = Int.MAX_VALUE,
    override val isReady: Boolean = true,
    override val activeModelId: String? = "gemma3-1b",
) : InferenceEngine {

    var lastImageBytes: ByteArray? = null
        private set

    private var emittedCount = 0

    override fun sendMessage(text: String): Flow<String> = flow {
        emittedCount = 0
        for (token in tokens) {
            if (shouldThrow && emittedCount >= throwAfterTokens) {
                throw RuntimeException("Simulated inference failure")
            }
            emit(token)
            emittedCount++
        }
    }

    override fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String> = flow {
        lastImageBytes = imageBytes
        emittedCount = 0
        for (token in tokens) {
            if (shouldThrow && emittedCount >= throwAfterTokens) {
                throw RuntimeException("Simulated inference failure")
            }
            emit(token)
            emittedCount++
        }
    }

    override fun stopGeneration() {}
}