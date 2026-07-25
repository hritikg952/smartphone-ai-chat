package com.smartphoneaichat.domain.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileAccessTest {

    @Test
    fun context_allowsOnlyDeclaredCapabilitiesForItsSelectedProfile() {
        val context = AuthorizedSessionContext(
            actorId = "owner",
            profileId = "self",
            sessionId = "session-1",
            role = ProfileRole.Self,
            capabilities = setOf(ProfileCapability.Read, ProfileCapability.Write),
        )

        assertTrue(context.can(ProfileCapability.Read))
        assertTrue(context.can(ProfileCapability.Write))
        assertFalse(context.can(ProfileCapability.Export))
        assertTrue(context.includesProfile("self"))
        assertFalse(context.includesProfile("another-profile"))
    }
}
