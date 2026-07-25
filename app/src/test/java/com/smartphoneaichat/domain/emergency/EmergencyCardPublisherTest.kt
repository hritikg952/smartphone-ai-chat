package com.smartphoneaichat.domain.emergency

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.model.VaultSessionState
import com.smartphoneaichat.domain.repository.EmergencyCardRepository
import com.smartphoneaichat.domain.repository.EmergencyCardReadResult
import com.smartphoneaichat.domain.repository.VaultSession
import com.smartphoneaichat.domain.usecase.EmergencyCardPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class EmergencyCardPublisherTest {

    @Test
    fun publish_copiesOnlyTheExplicitSelfProfileNameAndMarksLaterChangesAsAvailable() {
        val repository = FakeEmergencyCardRepository()
        val publisher = EmergencyCardPublisher(repository, FakeVaultSession(VaultSessionState.Unlocked), nowEpochMillis = { 500L })
        val context = context()

        publisher.publish(context, preferredName = "Avery")

        assertEquals(
            EmergencyCardProjection(
                profileId = "self",
                preferredName = "Avery",
                schemaVersion = 1,
                publishedAtEpochMillis = 500L,
                lastRefreshedAtEpochMillis = 500L,
            ),
            (repository.publicCard() as EmergencyCardReadResult.Available).projection,
        )
        assertEquals(
            EmergencyCardPublisher.State.UpdateAvailable(
                EmergencyCardProjection("self", "Avery", 1, 500L, 500L),
            ),
            publisher.stateFor(context, preferredName = "Avery Updated"),
        )
        assertEquals("Avery", (repository.publicCard() as EmergencyCardReadResult.Available).projection.preferredName)
    }

    @Test
    fun publishAndRevoke_requireAnActivelyUnlockedVaultEvenWithAValidRetainedContext() {
        val repository = FakeEmergencyCardRepository()
        val vaultSession = FakeVaultSession(VaultSessionState.Unlocked)
        val publisher = EmergencyCardPublisher(repository, vaultSession, nowEpochMillis = { 500L })
        val context = context()
        publisher.publish(context, "Avery")
        vaultSession.lock()

        assertThrows<IllegalStateException> { publisher.publish(context, "Updated Avery") }
        assertThrows<IllegalStateException> { publisher.revoke(context) }
        assertEquals("Avery", (repository.publicCard() as EmergencyCardReadResult.Available).projection.preferredName)
    }

    private fun context() = AuthorizedSessionContext(
        actorId = "vault-owner",
        profileId = "self",
        sessionId = "session",
        role = ProfileRole.Self,
        capabilities = setOf(ProfileCapability.ManageEmergencyProjection),
    )

    private class FakeEmergencyCardRepository : EmergencyCardRepository {
        private var projection: EmergencyCardProjection? = null

        override fun publicCard(): EmergencyCardReadResult = projection?.let(EmergencyCardReadResult::Available)
            ?: EmergencyCardReadResult.NotPublished

        override fun publish(context: AuthorizedSessionContext, projection: EmergencyCardProjection) {
            context.requireAccess(projection.profileId, ProfileCapability.ManageEmergencyProjection)
            this.projection = projection
        }

        override fun revoke(context: AuthorizedSessionContext) {
            context.requireAccess(context.profileId, ProfileCapability.ManageEmergencyProjection)
            projection = null
        }
    }

    private class FakeVaultSession(initialState: VaultSessionState) : VaultSession {
        private val mutableState = MutableStateFlow(initialState)
        override val state: StateFlow<VaultSessionState> = mutableState

        override fun lock() {
            mutableState.value = VaultSessionState.Locked
        }
    }
}
