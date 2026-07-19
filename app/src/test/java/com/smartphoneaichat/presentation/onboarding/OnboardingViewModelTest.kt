package com.smartphoneaichat.presentation.onboarding

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OnboardingViewModelTest {

    @Test
    fun onGetStarted_movesFromWelcomeToCredentials() {
        val viewModel = OnboardingViewModel()
        assertEquals(OnboardingStep.Welcome, viewModel.state.value.step)

        viewModel.onGetStarted()

        assertEquals(OnboardingStep.Credentials, viewModel.state.value.step)
    }
}
