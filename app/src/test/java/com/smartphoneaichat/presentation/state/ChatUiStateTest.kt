package com.smartphoneaichat.presentation.state

import com.smartphoneaichat.data.id.FakeIdGenerator
import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatUiStateTest {

    private val idGen = FakeIdGenerator()

    @Test
    fun activeConversation_returnsMatchingConversation() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = conv.id
        )
        assertEquals(conv, state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenNoMatch() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val otherId = ConversationId("conv-nonexistent")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = otherId
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenConversationsEmpty() {
        val state = ChatUiState(
            conversations = emptyList(),
            activeConversationId = idGen.generateConversationId()
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsNull_whenActiveIdIsNull() {
        val conv = Conversation.create(idGen.generateConversationId(), "Test")
        val state = ChatUiState(
            conversations = listOf(conv),
            activeConversationId = null
        )
        assertNull(state.activeConversation)
    }

    @Test
    fun activeConversation_returnsFirstMatchingConversation() {
        val conv1 = Conversation.create(idGen.generateConversationId(), "A")
        val conv2 = Conversation.create(idGen.generateConversationId(), "B")
        val state = ChatUiState(
            conversations = listOf(conv1, conv2),
            activeConversationId = conv2.id
        )
        assertEquals(conv2, state.activeConversation)
    }
}