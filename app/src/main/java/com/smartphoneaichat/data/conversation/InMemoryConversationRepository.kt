package com.smartphoneaichat.data.conversation

import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.repository.ConversationRepository
import java.util.concurrent.ConcurrentHashMap

class InMemoryConversationRepository : ConversationRepository {

    private val store = ConcurrentHashMap<ConversationId, Conversation>()

    override suspend fun getAll(): List<Conversation> = store.values.toList()

    override suspend fun getById(id: ConversationId): Conversation? = store[id]

    override suspend fun save(conversation: Conversation) {
        store[conversation.id] = conversation
    }

    override suspend fun delete(id: ConversationId) {
        store.remove(id)
    }
}