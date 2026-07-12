package com.smartphoneaichat.domain.model

import com.smartphoneaichat.domain.model.value.MessageId
import com.smartphoneaichat.domain.model.value.MessageText

data class Message(
    val role: ChatRole,
    val text: MessageText = MessageText(""),
    val thinkingText: String = "",
    val isStreaming: Boolean = false,
    val attachment: Attachment? = null,
    val id: MessageId
)

enum class ChatRole { USER, AI }

data class Attachment(
    val fileName: String,
    val mimeType: String,
    val imageUri: String? = null,
)