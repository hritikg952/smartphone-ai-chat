package com.smartphoneaichat.domain.usecase

import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.repository.EmergencyCardRepository
import com.smartphoneaichat.domain.repository.EmergencyCardReadResult
import com.smartphoneaichat.domain.repository.VaultSession
import com.smartphoneaichat.domain.model.VaultSessionState

/** Publishes a copy of the selected self profile name only after an explicit UI confirmation. */
class EmergencyCardPublisher(
    private val repository: EmergencyCardRepository,
    private val vaultSession: VaultSession,
    private val nowEpochMillis: () -> Long = { System.currentTimeMillis() },
) {
    sealed interface State {
        data object NotPublished : State
        data class Published(val projection: EmergencyCardProjection) : State
        data class UpdateAvailable(val projection: EmergencyCardProjection) : State
    }

    fun stateFor(context: AuthorizedSessionContext, preferredName: String): State {
        context.requireAccess(context.profileId, ProfileCapability.ManageEmergencyProjection)
        val projection = (repository.publicCard() as? EmergencyCardReadResult.Available)?.projection ?: return State.NotPublished
        if (projection.profileId != context.profileId || projection.preferredName != preferredName.trim()) {
            return State.UpdateAvailable(projection)
        }
        return State.Published(projection)
    }

    fun publish(context: AuthorizedSessionContext, preferredName: String): EmergencyCardProjection {
        requireUnlockedVault()
        context.requireAccess(context.profileId, ProfileCapability.ManageEmergencyProjection)
        val now = nowEpochMillis()
        val existing = (repository.publicCard() as? EmergencyCardReadResult.Available)?.projection
        val projection = EmergencyCardProjection(
            profileId = context.profileId,
            preferredName = preferredName.trim(),
            schemaVersion = CURRENT_SCHEMA_VERSION,
            publishedAtEpochMillis = existing?.publishedAtEpochMillis ?: now,
            lastRefreshedAtEpochMillis = now,
        )
        repository.publish(context, projection)
        return projection
    }

    fun revoke(context: AuthorizedSessionContext) {
        requireUnlockedVault()
        context.requireAccess(context.profileId, ProfileCapability.ManageEmergencyProjection)
        repository.revoke(context)
    }

    private fun requireUnlockedVault() {
        check(vaultSession.state.value == VaultSessionState.Unlocked) {
            "The vault must be unlocked to change the emergency card."
        }
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}
