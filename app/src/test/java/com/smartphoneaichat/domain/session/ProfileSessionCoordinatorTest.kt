package com.smartphoneaichat.domain.session

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.model.ProfileStateInvalidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ProfileSessionCoordinatorTest {

    @Test
    fun switchingClearsRegisteredFeatureStateBeforeExposingTheNextContext() {
        val order = mutableListOf<String>()
        val coordinator = ProfileSessionCoordinator(
            initialContext = context("self", "session-1"),
            invalidators = listOf(
                ProfileStateInvalidator { order += "clear" },
            ),
        )

        coordinator.switchTo(context("self", "session-2"))

        assertEquals(listOf("clear"), order)
        assertEquals("session-2", coordinator.currentContext.value?.sessionId)
    }

    @Test
    fun lockingClearsSelectedContextAndFeatureState() {
        var cleared = false
        val coordinator = ProfileSessionCoordinator(
            initialContext = context("self", "session-1"),
            invalidators = listOf(ProfileStateInvalidator { cleared = true }),
        )

        coordinator.clear()

        assertTrue(cleared)
        assertEquals(null, coordinator.currentContext.value)
    }

    private fun context(profileId: String, sessionId: String): AuthorizedSessionContext =
        AuthorizedSessionContext(
            actorId = "owner",
            profileId = profileId,
            sessionId = sessionId,
            role = ProfileRole.Self,
            capabilities = ProfileCapability.entries.toSet(),
        )
}
