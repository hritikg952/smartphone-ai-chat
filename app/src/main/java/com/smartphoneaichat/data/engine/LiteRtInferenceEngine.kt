package com.smartphoneaichat.data.engine

import com.google.ai.edge.litertlm.Content
import com.google.ai.edge.litertlm.Contents
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
import com.smartphoneaichat.domain.repository.InferenceEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LiteRtInferenceEngine(
    private val modelFileManager: HuggingFaceModelFileManager
) : InferenceEngine {

    override val isReady: Boolean
        get() = modelFileManager.isInitialized

    override val activeModelId: String?
        get() = modelFileManager.activeModelId

    override fun sendMessage(text: String): Flow<String> {
        val eng = modelFileManager.getEngine()
            ?: throw IllegalStateException("Engine not initialized. Call loadModel() first.")

        val conversation = eng.createConversation()

        return conversation.sendMessageAsync(text).map { liteRtMsg ->
            liteRtMsg.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
        }
    }

    override fun stopGeneration() {
    }

    override fun sendMultimodalMessage(text: String, imageBytes: ByteArray): Flow<String> {
        val eng = modelFileManager.getEngine()
            ?: throw IllegalStateException("Engine not initialized. Call loadModel() first.")

        val conversation = eng.createConversation()

        val contents = Contents.of(Content.Text(text), Content.ImageBytes(imageBytes))
        return conversation.sendMessageAsync(contents).map { liteRtMsg ->
            liteRtMsg.contents.contents.filterIsInstance<Content.Text>().joinToString("") { it.text }
        }
    }
}