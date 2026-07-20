package com.smartphoneaichat.data.session

import com.smartphoneaichat.domain.model.AppSessionState
import com.smartphoneaichat.domain.repository.AppSessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Small persistence seam for the only session value that survives a process restart. */
interface OnboardingStatusStorage {
    fun readHasCompletedOnboarding(): Boolean
    fun writeHasCompletedOnboarding(value: Boolean)
}

class DefaultAppSessionStore(
    private val onboardingStatusStorage: OnboardingStatusStorage,
) : AppSessionStore {

    private val _state = MutableStateFlow(
        AppSessionState(
            hasCompletedOnboarding = onboardingStatusStorage.readHasCompletedOnboarding(),
            isVaultUnlocked = false,
        ),
    )
    override val state: StateFlow<AppSessionState> = _state.asStateFlow()

    override fun completeOnboarding() {
        onboardingStatusStorage.writeHasCompletedOnboarding(true)
        _state.value = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = true,
        )
    }

    override fun unlockVault() {
        if (_state.value.hasCompletedOnboarding) {
            _state.value = _state.value.copy(isVaultUnlocked = true)
        }
    }

    override fun lockVault() {
        _state.value = _state.value.copy(isVaultUnlocked = false)
    }
}
