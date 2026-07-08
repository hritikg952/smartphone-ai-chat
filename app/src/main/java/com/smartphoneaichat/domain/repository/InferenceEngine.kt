package com.smartphoneaichat.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the LLM inference backend.
 *
 * Business rationale: Decouples chat logic from the specific on-device
 * runtime. Enables testing with fake engines and future migration to
 * cloud or other local APIs without changing domain code.
 *
 * ISP note: Contains ONLY inference concerns. Download and file
 * management are separated into [ModelFileManager].
 */
interface InferenceEngine {
    fun sendMessage(text: String): Flow<String>
    fun stopGeneration()
    val isReady: Boolean
    val activeModelId: String?
}