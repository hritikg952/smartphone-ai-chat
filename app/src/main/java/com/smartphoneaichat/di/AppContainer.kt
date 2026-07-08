package com.smartphoneaichat.di

import android.app.Application
import com.smartphoneaichat.data.conversation.InMemoryConversationRepository
import com.smartphoneaichat.data.engine.LiteRtInferenceEngine
import com.smartphoneaichat.data.id.UuidIdGenerator
import com.smartphoneaichat.data.model.HuggingFaceModelFileManager
import com.smartphoneaichat.domain.repository.ConversationRepository
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.repository.ModelFileManager
import com.smartphoneaichat.domain.service.ConversationTitleService
import com.smartphoneaichat.domain.usecase.DownloadModelUseCase
import com.smartphoneaichat.domain.usecase.LoadModelUseCase
import com.smartphoneaichat.domain.usecase.SendMessageUseCase

class AppContainer(private val application: Application) {

    val idGenerator: IdGenerator = UuidIdGenerator()

    val modelFileManager: ModelFileManager =
        HuggingFaceModelFileManager(application)

    val inferenceEngine: InferenceEngine =
        LiteRtInferenceEngine(modelFileManager as HuggingFaceModelFileManager)

    val titleService = ConversationTitleService

    val sendMessageUseCase = SendMessageUseCase(inferenceEngine, idGenerator, titleService)

    val downloadModelUseCase = DownloadModelUseCase(modelFileManager)

    val loadModelUseCase = LoadModelUseCase(modelFileManager)

    val conversationRepository: ConversationRepository =
        InMemoryConversationRepository()
}