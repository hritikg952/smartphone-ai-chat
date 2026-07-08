package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId

interface IdGenerator {
    fun generateMessageId(): MessageId
    fun generateConversationId(): ConversationId
}