package com.smartphoneaichat.domain.service

import com.smartphoneaichat.domain.model.value.MessageText

object ConversationTitleService {
    private const val MAX_TITLE_LENGTH = 40

    fun generateTitle(firstMessage: MessageText): String {
        val raw = firstMessage.value.trim()
        return if (raw.length <= MAX_TITLE_LENGTH) raw
        else raw.take(MAX_TITLE_LENGTH) + "\u2026"
    }
}