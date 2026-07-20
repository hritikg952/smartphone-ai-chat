package com.smartphoneaichat.data.session

import com.smartphoneaichat.domain.model.AppSessionState
import com.smartphoneaichat.domain.repository.AppSessionStore
import com.smartphoneaichat.domain.repository.VaultSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Small persistence seam for the only session value that survives a process restart. */
interface OnboardingStatusStorage {
    fun readHasCompletedOnboarding(): Boolean
    fun writeHasCompletedOnboarding(value: Boolean)
}

class DefaultAppSessionStore(
    private val onboardingStatusStorage: OnboardingStatusStorage,
    private val vaultSession: VaultSession,
    scope: CoroutineScope,
) : AppSessionStore {

    private val hasCompletedOnboarding = MutableStateFlow(
        onboardingStatusStorage.readHasCompletedOnboarding(),
    )
    override val state: StateFlow<AppSessionState> = combine(
        hasCompletedOnboarding,
        vaultSession.state,
    ) { onboardingComplete, vaultState ->
        AppSessionState(
            hasCompletedOnboarding = onboardingComplete && vaultState.hasVaultEnvelope(),
            isVaultUnlocked = vaultState == com.smartphoneaichat.domain.model.VaultSessionState.Unlocked,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AppSessionState(
            hasCompletedOnboarding = hasCompletedOnboarding.value &&
                vaultSession.state.value.hasVaultEnvelope(),
            isVaultUnlocked = vaultSession.state.value ==
                com.smartphoneaichat.domain.model.VaultSessionState.Unlocked,
        ),
    )

    override fun completeOnboarding() {
        if (vaultSession.state.value != com.smartphoneaichat.domain.model.VaultSessionState.Unlocked) {
            return
        }
        onboardingStatusStorage.writeHasCompletedOnboarding(true)
        hasCompletedOnboarding.value = true
    }

}

private fun com.smartphoneaichat.domain.model.VaultSessionState.hasVaultEnvelope(): Boolean =
    this != com.smartphoneaichat.domain.model.VaultSessionState.Absent &&
        this != com.smartphoneaichat.domain.model.VaultSessionState.Destroyed
