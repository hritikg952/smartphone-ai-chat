package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection

sealed interface EmergencyCardReadResult {
    data object NotPublished : EmergencyCardReadResult
    data object Unavailable : EmergencyCardReadResult
    data class Available(val projection: EmergencyCardProjection) : EmergencyCardReadResult
}

/** Separate persistence boundary for the intentionally public emergency-card projection. */
interface EmergencyCardRepository {
    /** Reads only the independently authenticated public snapshot; it never needs a vault session. */
    fun publicCard(): EmergencyCardReadResult

    fun publish(context: AuthorizedSessionContext, projection: EmergencyCardProjection)

    fun revoke(context: AuthorizedSessionContext)
}
