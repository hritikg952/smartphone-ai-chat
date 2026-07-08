package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.Conversation
import com.smartphoneaichat.domain.model.value.ConversationId

/**
 * Persistence abstraction for conversation threads.
 *
 * Business rationale: Conversations are currently in-memory but the
 * codebase has explicit plans for Room/DataStore persistence.
 * Abstracting this now avoids a second refactoring later.
 *
 * The ViewModel never touches persistence details — it works through
 * this interface whether the implementation is in-memory or SQLite.
 */
interface ConversationRepository {
    suspend fun getAll(): List<Conversation>
    suspend fun getById(id: ConversationId): Conversation?
    suspend fun save(conversation: Conversation)
    suspend fun delete(id: ConversationId)
}