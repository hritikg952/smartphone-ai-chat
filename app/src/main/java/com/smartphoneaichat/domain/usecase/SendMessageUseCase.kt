package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.ChatRole
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.Message
import com.smartphoneaichat.domain.model.value.MessageText
import com.smartphoneaichat.domain.repository.IdGenerator
import com.smartphoneaichat.domain.repository.InferenceEngine
import com.smartphoneaichat.domain.service.ConversationTitleService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SendMessageUseCase(
    private val inferenceEngine: InferenceEngine,
    private val idGenerator: IdGenerator,
    private val titleService: ConversationTitleService = ConversationTitleService,
) {
    operator fun invoke(conversation: Conversation, text: MessageText): Flow<Conversation> = flow {
        val userMessage = Message(
            id = idGenerator.generateMessageId(),
            role = ChatRole.USER,
            text = text
        )
        val aiMessageId = idGenerator.generateMessageId()
        val aiMessage = Message(
            id = aiMessageId,
            role = ChatRole.AI,
            text = MessageText(""),
            thinkingText = "Analyzing your request...\n" +
                    "Identifying key concepts...\n" +
                    "Formulating response...",
            isStreaming = true
        )

        var currentConv = conversation
            .addMessage(userMessage)
            .addMessage(aiMessage)

        val shouldAutoTitle = conversation.title == "New Chat" && conversation.messages.size <= 1
        if (shouldAutoTitle) {
            currentConv = currentConv.withTitle(titleService.generateTitle(text))
        }

        emit(currentConv)

        try {
            inferenceEngine.sendMessage(text.value).collect { token ->
                currentConv = currentConv.updateMessage(aiMessageId) { msg ->
                    msg.copy(text = MessageText(msg.text.value + token))
                }
                emit(currentConv)
            }
        } finally {
            currentConv = currentConv.updateMessage(aiMessageId) { msg ->
                msg.copy(isStreaming = false)
            }
            try {
                emit(currentConv)
            } catch (_: CancellationException) {
            }
        }
    }
}