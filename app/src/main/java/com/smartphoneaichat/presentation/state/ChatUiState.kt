package com.smartphoneaichat.presentation.state

import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.ModelInfo
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: ConversationId? = null,
    val isSidebarOpen: Boolean = false,
    val thinkingExpandedIds: Set<MessageId> = emptySet(),
    val loadingModelId: String? = null,
    val isModelLoading: Boolean = false,
    val modelLoadProgress: Float = 0f,
    val modelLoadPhase: String = "",
    val activeModelId: String? = null,
    val activeModelDisplayName: String? = null,
    val downloadedModelIds: Set<String> = emptySet(),
    val showDeleteConfirmation: Boolean = false,
    val deleteTargetModelId: String? = null,
    val showModelSelector: Boolean = false,
    val modelSelectorModels: List<ModelInfo> = emptyList(),
    val pendingAttachmentUri: String? = null,
) {
    val activeConversation: Conversation?
        get() = conversations.find { it.id == activeConversationId }
}