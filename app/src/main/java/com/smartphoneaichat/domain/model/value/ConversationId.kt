package com.smartphoneaichat.domain.model.value

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@JvmInline
value class ConversationId(val value: String) : Parcelable {
    init {
        require(value.isNotBlank()) { "ConversationId must not be blank" }
    }
}
