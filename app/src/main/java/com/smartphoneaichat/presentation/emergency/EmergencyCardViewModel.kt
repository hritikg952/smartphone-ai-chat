package com.smartphoneaichat.presentation.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.AuditEventType
import com.smartphoneaichat.domain.model.AuditOutcome
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.domain.repository.AuditRepository
import com.smartphoneaichat.domain.repository.EmergencyCardRepository
import com.smartphoneaichat.domain.repository.EmergencyCardReadResult
import com.smartphoneaichat.domain.repository.ProfileRepository
import com.smartphoneaichat.domain.repository.VaultSession
import com.smartphoneaichat.domain.usecase.EmergencyCardPublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EmergencyCardUiState(
    val projection: EmergencyCardProjection? = null,
    val currentPreferredName: String? = null,
    val isUpdateAvailable: Boolean = false,
    val isUnavailable: Boolean = false,
    val showExposureWarning: Boolean = false,
)

class EmergencyCardViewModelFactory(
    private val emergencyCards: EmergencyCardRepository,
    private val profiles: ProfileRepository,
    private val audit: AuditRepository,
    private val vaultSession: VaultSession,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(EmergencyCardViewModel::class.java))
        return EmergencyCardViewModel(emergencyCards, profiles, audit, vaultSession) as T
    }
}

/** Keeps vault-only profile reads out of the public emergency-card render path. */
class EmergencyCardViewModel(
    private val emergencyCards: EmergencyCardRepository,
    private val profiles: ProfileRepository,
    private val audit: AuditRepository,
    vaultSession: VaultSession,
    private val nowEpochMillis: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {
    private val publisher = EmergencyCardPublisher(emergencyCards, vaultSession, nowEpochMillis)
    private val _state = MutableStateFlow(EmergencyCardUiState())
    val state: StateFlow<EmergencyCardUiState> = _state.asStateFlow()

    fun refresh(isVaultUnlocked: Boolean, context: AuthorizedSessionContext?) {
        val readResult = emergencyCards.publicCard()
        val projection = (readResult as? EmergencyCardReadResult.Available)?.projection
        if (!isVaultUnlocked || context == null) {
            _state.value = EmergencyCardUiState(
                projection = projection,
                isUnavailable = readResult is EmergencyCardReadResult.Unavailable,
            )
            return
        }
        val preferredName = profiles.get(context)?.displayName
        val publisherState = preferredName?.let { publisher.stateFor(context, it) }
        _state.value = EmergencyCardUiState(
            projection = projection,
            currentPreferredName = preferredName,
            isUpdateAvailable = publisherState is EmergencyCardPublisher.State.UpdateAvailable,
            isUnavailable = readResult is EmergencyCardReadResult.Unavailable,
        )
    }

    fun requestPublish() {
        if (_state.value.currentPreferredName != null) {
            _state.value = _state.value.copy(showExposureWarning = true)
        }
    }

    fun dismissExposureWarning() {
        _state.value = _state.value.copy(showExposureWarning = false)
    }

    fun confirmPublish(context: AuthorizedSessionContext?) {
        val name = _state.value.currentPreferredName ?: return
        if (context == null || !_state.value.showExposureWarning) return
        publisher.publish(context, name)
        record(context, "published")
        refresh(isVaultUnlocked = true, context = context)
    }

    fun revoke(context: AuthorizedSessionContext?) {
        if (context == null) return
        publisher.revoke(context)
        record(context, "revoked")
        refresh(isVaultUnlocked = true, context = context)
    }

    private fun record(context: AuthorizedSessionContext, detailCode: String) {
        audit.append(
            AuditEvent(
                eventId = "emergency-card:${nowEpochMillis()}:$detailCode",
                actorId = context.actorId,
                profileId = context.profileId,
                type = AuditEventType.AdministrativeAction,
                outcome = AuditOutcome.Success,
                occurredAtEpochMillis = nowEpochMillis(),
                targetType = "emergency_card",
                targetId = null,
                detailCode = detailCode,
            ),
        )
    }
}
