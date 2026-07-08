package com.smartphoneaichat.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartphoneaichat.domain.model.Attachment
import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.modelInfoById
import com.smartphoneaichat.domain.usecase.DownloadModelUseCase
import com.smartphoneaichat.domain.usecase.LoadModelUseCase
import com.smartphoneaichat.domain.usecase.SendMessageUseCase
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId
import com.smartphoneaichat.domain.model.value.MessageText
import com.smartphoneaichat.domain.repository.ConversationRepository
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.repository.ModelFileManager
import com.smartphoneaichat.presentation.notification.AppNotificationEvent
import com.smartphoneaichat.presentation.notification.AppNotificationManager
import com.smartphoneaichat.presentation.state.ChatUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val downloadModelUseCase: DownloadModelUseCase,
    private val loadModelUseCase: LoadModelUseCase,
    private val inferenceEngine: InferenceEngine,
    private val modelFileManager: ModelFileManager,
    private val conversationRepository: ConversationRepository,
    private val idGenerator: IdGenerator,
    application: Application,
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private var streamingJob: Job? = null
    private var downloadJob: Job? = null

    val notifications = AppNotificationManager()

    init {
        viewModelScope.launch {
            val existing = conversationRepository.getAll()
            if (existing.isNotEmpty()) {
                _state.update {
                    it.copy(
                        conversations = existing,
                        activeConversationId = existing.first().id
                    )
                }
            } else {
                val mockConversation = Conversation.create(
                    id = idGenerator.generateConversationId(),
                    title = "Welcome to AI Chat"
                ).addMessage(
                    Message(
                        id = idGenerator.generateMessageId(),
                        role = ChatRole.AI,
                        text = MessageText("Hello! I'm your AI assistant. Send me a message to get started."),
                        thinkingText = "",
                    )
                )
                conversationRepository.save(mockConversation)
                _state.update {
                    it.copy(
                        conversations = listOf(mockConversation),
                        activeConversationId = mockConversation.id
                    )
                }
            }
            refreshDownloadedModels()
        }
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
        modelFileManager.unloadModel()
    }

    private fun refreshDownloadedModels() {
        val downloaded = modelFileManager.listDownloadedModelIds().toSet()
        _state.update { it.copy(downloadedModelIds = downloaded) }
    }

    // ── Sidebar ──────────────────────────────────────────────────

    fun toggleSidebar() {
        _state.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
    }

    fun closeSidebar() {
        _state.update { it.copy(isSidebarOpen = false) }
    }

    // ── Send Message ─────────────────────────────────────────────

    companion object {
        const val MAX_INPUT_LENGTH = 4096
    }

    fun sendMessage(text: String) {
        val trimmedInput = text.trim()
        if (trimmedInput.isEmpty()) return

        if (trimmedInput.length > MAX_INPUT_LENGTH) {
            notifications.show(AppNotificationEvent.Error("Message too long (max $MAX_INPUT_LENGTH characters)"))
            return
        }

        streamingJob?.cancel()

        val currentConv = _state.value.activeConversation ?: return

        streamingJob = viewModelScope.launch {
            try {
                sendMessageUseCase(currentConv, MessageText(trimmedInput))
                    .flowOn(Dispatchers.IO)
                    .collect { updatedConversation ->
                        _state.update { replaceConversation(updatedConversation) }
                        conversationRepository.save(updatedConversation)
                    }
            } catch (e: Exception) {
                notifications.show(AppNotificationEvent.Error("AI error: ${e.message}"))
            }
        }
    }

    // ── Image Attachment ─────────────────────────────────────────

    fun attachImage() {
        val stateSnapshot = _state.value
        val conv = stateSnapshot.activeConversation ?: return

        val attachment = Attachment(
            fileName = "photo_${System.currentTimeMillis() % 10000}.jpg",
            mimeType = "image/jpeg"
        )

        val pendingMessage = Message(
            id = idGenerator.generateMessageId(),
            role = ChatRole.USER,
            text = MessageText(""),
            attachment = attachment
        )

        val updatedConv = conv.addMessage(pendingMessage)

        _state.update { replaceConversation(updatedConv) }
        notifications.show(AppNotificationEvent.Success("Image attached: ${attachment.fileName}"))
    }

    fun removePendingAttachment() {
        _state.update { state ->
            val conv = state.activeConversation ?: return@update state
            val msgs = conv.messages
            if (msgs.isNotEmpty()) {
                val last = msgs.last()
                if (last.role == ChatRole.USER && last.text.value.isEmpty() && last.attachment != null) {
                    return@update replaceConversation(
                        conv.replaceMessages(msgs.dropLast(1))
                    )
                }
            }
            state
        }
    }

    // ── Model Download ───────────────────────────────────────────

    fun downloadModel(modelId: String) {
        if (_state.value.isModelLoading) return

        val modelInfo = modelInfoById(modelId) ?: return

        downloadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isModelLoading = true,
                    modelLoadProgress = 0f,
                    modelLoadPhase = "Downloading ${modelInfo.displayName}...",
                    loadingModelId = modelInfo.id
                )
            }

            try {
                downloadModelUseCase(modelId) { progress ->
                    _state.update { it.copy(modelLoadProgress = progress) }
                }

                if (!isActive) return@launch

                refreshDownloadedModels()

                val downloaded = _state.value.downloadedModelIds
                if (downloaded.size == 1) {
                    _state.update { it.copy(loadingModelId = null) }
                    loadModelInner(modelId)
                } else {
                    _state.update {
                        it.copy(
                            isModelLoading = false,
                            modelLoadProgress = 0f,
                            modelLoadPhase = "",
                            loadingModelId = null
                        )
                    }
                    val downloadedModels = downloaded.mapNotNull { modelInfoById(it) }
                    if (downloadedModels.size >= 2) {
                        _state.update {
                            it.copy(
                                showModelSelector = true,
                                modelSelectorModels = downloadedModels
                            )
                        }
                    }
                    notifications.show(
                        AppNotificationEvent.Success("${modelInfo.displayName} downloaded successfully!")
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isModelLoading = false,
                        modelLoadProgress = 0f,
                        modelLoadPhase = "",
                        loadingModelId = null
                    )
                }
                notifications.show(AppNotificationEvent.Error("Download failed: ${e.message}"))
            }
        }
    }

    fun cancelModelDownload() {
        modelFileManager.cancelDownload()
        downloadJob?.cancel()
        downloadJob = null
        val loadId = _state.value.loadingModelId
        _state.update {
            it.copy(
                isModelLoading = false,
                modelLoadProgress = 0f,
                modelLoadPhase = "",
                loadingModelId = null
            )
        }
        if (loadId != null) {
            notifications.show(AppNotificationEvent.Success("Download cancelled"))
        }
    }

    // ── Model Loading ────────────────────────────────────────────

    fun loadModel(modelId: String) {
        if (_state.value.isModelLoading) return

        val downloaded = _state.value.downloadedModelIds
        if (downloaded.size >= 2) {
            val models = downloaded.mapNotNull { modelInfoById(it) }
            _state.update {
                it.copy(
                    showModelSelector = true,
                    modelSelectorModels = models
                )
            }
        } else {
            viewModelScope.launch {
                loadModelInner(modelId)
            }
        }
    }

    private suspend fun loadModelInner(modelId: String) {
        val modelInfo = modelInfoById(modelId) ?: return

        _state.update {
            it.copy(
                isModelLoading = true,
                modelLoadProgress = 0f,
                modelLoadPhase = "Initializing ${modelInfo.displayName}...",
                loadingModelId = modelInfo.id
            )
        }

        try {
            loadModelUseCase(modelId) { progress ->
                _state.update { it.copy(modelLoadProgress = progress) }
            }
            _state.update {
                it.copy(
                    isModelLoading = false,
                    modelLoadProgress = 0f,
                    modelLoadPhase = "",
                    activeModelId = modelInfo.id,
                    activeModelDisplayName = modelInfo.displayName,
                    loadingModelId = null
                )
            }
            notifications.show(
                AppNotificationEvent.Success("${modelInfo.displayName} loaded successfully!")
            )
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isModelLoading = false,
                    modelLoadProgress = 0f,
                    modelLoadPhase = "",
                    activeModelId = null,
                    activeModelDisplayName = null,
                    loadingModelId = null
                )
            }
            notifications.show(AppNotificationEvent.Error("Failed to load model: ${e.message}"))
        }
    }

    fun selectModelFromSelector(modelId: String) {
        _state.update {
            it.copy(
                showModelSelector = false,
                modelSelectorModels = emptyList()
            )
        }
        viewModelScope.launch {
            loadModelInner(modelId)
        }
    }

    fun dismissModelSelector() {
        _state.update {
            it.copy(
                showModelSelector = false,
                modelSelectorModels = emptyList()
            )
        }
    }

    fun unloadModel() {
        if (!inferenceEngine.isReady) return
        val modelInfo = _state.value.activeModelId?.let { modelInfoById(it) }
        modelFileManager.unloadModel()
        _state.update {
            it.copy(
                activeModelId = null,
                activeModelDisplayName = null
            )
        }
        modelInfo?.let { info ->
            notifications.show(
                AppNotificationEvent.Success("${info.displayName} unloaded from memory")
            )
        }
    }

    // ── Delete Model ─────────────────────────────────────────────

    fun confirmDeleteModel(modelId: String) {
        _state.update {
            it.copy(showDeleteConfirmation = true, deleteTargetModelId = modelId)
        }
    }

    fun cancelDeleteModel() {
        _state.update {
            it.copy(showDeleteConfirmation = false, deleteTargetModelId = null)
        }
    }

    fun deleteModel() {
        val targetId = _state.value.deleteTargetModelId ?: return
        val modelInfo = modelInfoById(targetId) ?: return

        _state.update { it.copy(showDeleteConfirmation = false, deleteTargetModelId = null) }

        viewModelScope.launch {
            modelFileManager.deleteModelFile(modelInfo)
            if (_state.value.activeModelId == targetId) {
                _state.update {
                    it.copy(activeModelId = null, activeModelDisplayName = null)
                }
            }
            refreshDownloadedModels()
            notifications.show(AppNotificationEvent.Success("${modelInfo.displayName} deleted"))
        }
    }

    // ── Conversation Management ──────────────────────────────────

    fun selectConversation(conversationId: ConversationId) {
        streamingJob?.cancel()
        _state.update {
            it.copy(
                activeConversationId = conversationId,
                isSidebarOpen = false
            )
        }
    }

    fun newConversation() {
        streamingJob?.cancel()
        val newConv = Conversation.create(
            id = idGenerator.generateConversationId(),
            title = "New Chat"
        )
        viewModelScope.launch {
            conversationRepository.save(newConv)
        }
        _state.update {
            it.copy(
                conversations = it.conversations + newConv,
                activeConversationId = newConv.id,
                isSidebarOpen = false
            )
        }
    }

    fun deleteConversation(conversationId: ConversationId) {
        viewModelScope.launch {
            conversationRepository.delete(conversationId)
        }
        _state.update { state ->
            val remaining = state.conversations.filter { it.id != conversationId }
            if (remaining.isEmpty()) {
                val fresh = Conversation.create(id = idGenerator.generateConversationId())
                viewModelScope.launch {
                    conversationRepository.save(fresh)
                }
                state.copy(
                    conversations = listOf(fresh),
                    activeConversationId = fresh.id
                )
            } else {
                val newActiveId = if (state.activeConversationId == conversationId) {
                    remaining.first().id
                } else {
                    state.activeConversationId
                }
                state.copy(
                    conversations = remaining,
                    activeConversationId = newActiveId
                )
            }
        }
        notifications.show(AppNotificationEvent.Success("Conversation deleted"))
    }

    // ── Thinking Toggle ──────────────────────────────────────────

    fun toggleThinkingExpanded(messageId: MessageId) {
        _state.update { state ->
            val current = state.thinkingExpandedIds.toMutableSet()
            if (current.contains(messageId)) current.remove(messageId) else current.add(messageId)
            state.copy(thinkingExpandedIds = current)
        }
    }

    // ── Helpers ──────────────────────────────────────────────────

    private fun ChatUiState.replaceConversation(updated: Conversation): ChatUiState {
        return copy(
            conversations = conversations.map { conv ->
                if (conv.id == updated.id) updated else conv
            }
        )
    }

    private fun replaceConversation(updated: Conversation): ChatUiState {
        return _state.value.replaceConversation(updated)
    }
}