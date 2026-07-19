package com.smartphoneaichat.ui.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.smartphoneaichat.presentation.onboarding.OnboardingViewModel
import com.smartphoneaichat.presentation.session.AppSessionState
import org.junit.Rule
import org.junit.Test

class PersonalHealthVaultAppTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun freshSession_displaysOnboardingInsteadOfLegacyChat() {
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = false,
                    isVaultUnlocked = false,
                ),
            )
        }

        composeRule.onNodeWithText("Personal Health Vault").assertIsDisplayed()
        composeRule.onNodeWithText("Get started").assertIsDisplayed()
        composeRule.onNodeWithText("AI Chat").assertDoesNotExist()
    }

    @Test
    fun getStarted_displaysLocalAccessSetup() {
        val onboardingViewModel = OnboardingViewModel()

        composeRule.setContent {
            val onboardingState by onboardingViewModel.state.collectAsState()
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = false,
                    isVaultUnlocked = false,
                ),
                onboardingState = onboardingState,
                onGetStarted = onboardingViewModel::onGetStarted,
            )
        }

        composeRule.onNodeWithText("Get started").performClick()

        composeRule.onNodeWithText("Set up local access").assertIsDisplayed()
    }
}
