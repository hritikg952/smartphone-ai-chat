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

/**
 * Represents a file attached to a message (mocked).
 *
 * AI INTEGRATION NOTE:
 * In production this would hold a real [android.net.Uri], a MIME type, and a
 * file size. The ViewModel would use an InputStream to read the bytes before
 * sending them to the AI API (e.g., Gemini vision or GPT-4 Vision).
 */
data class Attachment(
    /** Display name of the attached file. */
    val fileName: String,

    /** Short MIME-type hint used to decide the thumbnail icon. */
    val mimeType: String
)