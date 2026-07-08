package com.smartphoneaichat.di

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartphoneaichat.presentation.viewmodel.ChatViewModel

class ChatViewModelFactory(
    private val container: AppContainer,
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(
            sendMessageUseCase = container.sendMessageUseCase,
            downloadModelUseCase = container.downloadModelUseCase,
            loadModelUseCase = container.loadModelUseCase,
            inferenceEngine = container.inferenceEngine,
            modelFileManager = container.modelFileManager,
            conversationRepository = container.conversationRepository,
            idGenerator = container.idGenerator,
            application = application,
        ) as T
    }
}