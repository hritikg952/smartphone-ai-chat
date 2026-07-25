package com.smartphoneaichat.domain.profile

import com.smartphoneaichat.data.governance.InMemoryProfileRepository
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.Profile
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ProfileRepositoryTest {

    @Test
    fun selfProfileCanBeInitializedAndReadOnlyThroughItsAuthorizedContext() {
        val repository = InMemoryProfileRepository()
        val profile = profile("self")
        repository.initializeSelfProfile(profile)

        assertEquals(profile, repository.get(context("self")))
        assertNull(repository.get(context("other")))
    }

    @Test
    fun initializationRejectsDependentRoleInTheSelfOnlyPrototype() {
        val repository = InMemoryProfileRepository()

        assertThrows(IllegalArgumentException::class.java) {
            repository.initializeSelfProfile(profile("dependent", ProfileRole.CaregiverEditor))
        }
    }

    private fun profile(id: String, role: ProfileRole = ProfileRole.Self): Profile = Profile(
        id = id,
        actorId = "owner",
        displayName = "Owner",
        relationship = role,
        createdAtEpochMillis = 1L,
        updatedAtEpochMillis = 1L,
    )

    private fun context(profileId: String): AuthorizedSessionContext = AuthorizedSessionContext(
        actorId = "owner",
        profileId = profileId,
        sessionId = "session-1",
        role = ProfileRole.Self,
        capabilities = ProfileCapability.entries.toSet(),
    )
}
