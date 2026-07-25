package com.smartphoneaichat.domain.model

enum class ProfileStatus {
    Active,
    Archived,
}

enum class DateOfBirthPrecision {
    Exact,
    YearOnly,
    Unknown,
}

/** A local health-record subject. The prototype creates only the self profile. */
data class Profile(
    val id: String,
    val actorId: String,
    val displayName: String,
    val relationship: ProfileRole,
    val dateOfBirthEpochMillis: Long? = null,
    val dateOfBirthPrecision: DateOfBirthPrecision = DateOfBirthPrecision.Unknown,
    val demographicFields: Map<String, String> = emptyMap(),
    val avatarReference: String? = null,
    val status: ProfileStatus = ProfileStatus.Active,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
) {
    init {
        require(id.isNotBlank())
        require(actorId.isNotBlank())
        require(displayName.isNotBlank())
        require(createdAtEpochMillis >= 0)
        require(updatedAtEpochMillis >= createdAtEpochMillis)
        require(relationship == ProfileRole.Self || relationship == ProfileRole.VaultOwner) {
            "The current prototype supports self profiles only."
        }
    }
}
