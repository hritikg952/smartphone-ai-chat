package com.smartphoneaichat.data.id

import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId
import com.smartphoneaichat.domain.repository.IdGenerator

class FakeIdGenerator : IdGenerator {
    private var messageCounter = 0
    private var conversationCounter = 0

    override fun generateMessageId(): MessageId {
        messageCounter++
        return MessageId("msg-$messageCounter")
    }

    override fun generateConversationId(): ConversationId {
        conversationCounter++
        return ConversationId("conv-$conversationCounter")
    }
}