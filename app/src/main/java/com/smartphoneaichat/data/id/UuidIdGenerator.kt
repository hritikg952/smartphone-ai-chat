package com.smartphoneaichat.data.id

import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId
import com.smartphoneaichat.domain.repository.IdGenerator
import java.util.UUID

class UuidIdGenerator : IdGenerator {
    override fun generateMessageId(): MessageId = MessageId("msg-${UUID.randomUUID()}")

    override fun generateConversationId(): ConversationId = ConversationId("conv-${UUID.randomUUID()}")
}