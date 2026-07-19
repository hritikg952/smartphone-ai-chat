package com.smartphoneaichat.presentation.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class OnboardingStep {
    Welcome,
    Credentials,
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Welcome,
)

/** Owns the state transitions for the first-run vault setup flow. */
class OnboardingViewModel : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onGetStarted() {
        _state.update { it.copy(step = OnboardingStep.Credentials) }
    }
}
