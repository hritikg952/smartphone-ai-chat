package com.smartphoneaichat.domain.model

import com.smartphoneaichat.domain.model.value.ConversationId
import com.smartphoneaichat.domain.model.value.MessageId

@ConsistentCopyVisibility
data class Conversation private constructor(
    val id: ConversationId,
    val title: String,
    val messages: List<Message>,
) {
    fun addMessage(message: Message): Conversation = copy(messages = messages + message)

    fun updateMessage(messageId: MessageId, transform: (Message) -> Message): Conversation =
        copy(messages = messages.map { if (it.id == messageId) transform(it) else it })

    fun replaceMessages(newMessages: List<Message>): Conversation = copy(messages = newMessages)

    fun withTitle(title: String): Conversation = copy(title = title)

    val lastMessage: Message? get() = messages.lastOrNull()
    val isEmpty: Boolean get() = messages.isEmpty()

    companion object {
        fun create(id: ConversationId, title: String = "New Chat"): Conversation =
            Conversation(id = id, title = title, messages = emptyList())
    }
}
