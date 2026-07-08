package com.smartphoneaichat.domain.model.value

@JvmInline
value class MessageText(val value: String) {
    init {
        require(value.length <= 4096) { "MessageText must not exceed 4096 characters" }
    }
}
