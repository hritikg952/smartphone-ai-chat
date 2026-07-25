package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.DateOfBirthPrecision
import com.smartphoneaichat.domain.model.Profile
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.repository.ProfileRepository

fun interface SelfProfileInitializer {
    fun ensureSelfProfile(displayName: String, nowEpochMillis: Long): Profile
}

class DefaultSelfProfileInitializer(
    private val repository: ProfileRepository,
) : SelfProfileInitializer {
    override fun ensureSelfProfile(displayName: String, nowEpochMillis: Long): Profile {
        repository.findSelfProfile()?.let { return it }
        return repository.initializeSelfProfile(
            Profile(
                id = SELF_PROFILE_ID,
                actorId = OWNER_ACTOR_ID,
                displayName = displayName.trim().ifBlank { "Me" },
                relationship = ProfileRole.Self,
                dateOfBirthPrecision = DateOfBirthPrecision.Unknown,
                createdAtEpochMillis = nowEpochMillis,
                updatedAtEpochMillis = nowEpochMillis,
            ),
        )
    }

    companion object {
        const val SELF_PROFILE_ID = "self"
        const val OWNER_ACTOR_ID = "vault-owner"
    }
}
