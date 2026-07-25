package com.smartphoneaichat.domain.model

/** The deliberately minimal, user-published snapshot visible without vault authentication. */
data class EmergencyCardProjection(
    val profileId: String,
    val preferredName: String,
    val schemaVersion: Int,
    val publishedAtEpochMillis: Long,
    val lastRefreshedAtEpochMillis: Long,
) {
    init {
        require(profileId.isNotBlank())
        require(preferredName.isNotBlank())
        require(schemaVersion > 0)
        require(publishedAtEpochMillis >= 0)
        require(lastRefreshedAtEpochMillis >= publishedAtEpochMillis)
    }
}
