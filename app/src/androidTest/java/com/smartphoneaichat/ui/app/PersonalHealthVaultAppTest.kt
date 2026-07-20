package com.smartphoneaichat.ui.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.smartphoneaichat.presentation.onboarding.OnboardingViewModel
import com.smartphoneaichat.presentation.session.AppSessionState
import com.smartphoneaichat.presentation.security.VaultAccessUiState
import com.smartphoneaichat.ui.navigation.AppRoute
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

    @Test
    fun completedLockedSessionDisplaysUnlock() {
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = true,
                    isVaultUnlocked = false,
                ),
            )
        }

        composeRule.onNodeWithText("Unlock your vault").assertIsDisplayed()
        composeRule.onNodeWithText("Home").assertDoesNotExist()
    }

    @Test
    fun completedUnlockedSessionDisplaysHome() {
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = true,
                    isVaultUnlocked = true,
                ),
            )
        }

        composeRule.onNodeWithText("Health Vault Home").assertIsDisplayed()
        composeRule.onNodeWithText("Unlock your vault").assertDoesNotExist()
    }

    @Test
    fun validPrototypeCredentialsCompleteOnboardingAndDisplayHome() {
        val onboardingViewModel = OnboardingViewModel()
        var sessionState by mutableStateOf(
            AppSessionState(
                hasCompletedOnboarding = false,
                isVaultUnlocked = false,
            ),
        )
        var vaultAccessState by mutableStateOf(VaultAccessUiState())

        composeRule.setContent {
            val onboardingState by onboardingViewModel.state.collectAsState()
            PersonalHealthVaultApp(
                sessionState = sessionState,
                onboardingState = onboardingState,
                vaultAccessState = vaultAccessState,
                onGetStarted = onboardingViewModel::onGetStarted,
                onUsernameChanged = {
                    vaultAccessState = vaultAccessState.copy(username = it)
                },
                onPasswordChanged = {
                    vaultAccessState = vaultAccessState.copy(password = it)
                },
                onCompleteOnboarding = {
                    sessionState = AppSessionState(
                        hasCompletedOnboarding = true,
                        isVaultUnlocked = true,
                    )
                },
            )
        }

        composeRule.onNodeWithText("Get started").performClick()
        composeRule.onNodeWithText("Username").performTextInput("owner")
        composeRule.onNodeWithText("Password").performTextInput("vault-pass")
        composeRule.onNodeWithText("Create vault").performClick()

        composeRule.onNodeWithText("Health Vault Home").assertIsDisplayed()
    }

    @Test
    fun unlockedUserCanOpenFeatureRouteAndReturnHome() {
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = true,
                    isVaultUnlocked = true,
                ),
            )
        }

        composeRule.onNodeWithText("Medications").performClick()
        composeRule.onNodeWithText("Medications").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()

        composeRule.onNodeWithText("Health Vault Home").assertIsDisplayed()
    }

    @Test
    fun lockingVaultRemovesProtectedHomeContent() {
        var sessionState by mutableStateOf(
            AppSessionState(
                hasCompletedOnboarding = true,
                isVaultUnlocked = true,
            ),
        )
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = sessionState,
                onLock = {
                    sessionState = sessionState.copy(isVaultUnlocked = false)
                },
            )
        }

        composeRule.onNodeWithText("Lock vault").performClick()

        composeRule.onNodeWithText("Unlock your vault").assertIsDisplayed()
        composeRule.onNodeWithText("Health Vault Home").assertDoesNotExist()
    }

    @Test
    fun everyTopLevelFeatureRouteIsAddressable() {
        composeRule.setContent {
            PersonalHealthVaultApp(
                sessionState = AppSessionState(
                    hasCompletedOnboarding = true,
                    isVaultUnlocked = true,
                ),
            )
        }

        AppRoute.protectedRoutes
            .filterNot { it == AppRoute.Home }
            .forEach { route ->
                composeRule.onNodeWithText(route.displayName)
                    .performScrollTo()
                    .performClick()
                composeRule.onNodeWithText(route.displayName).assertIsDisplayed()
                composeRule.onNodeWithText("Back").performClick()
                composeRule.onNodeWithText("Health Vault Home").assertIsDisplayed()
            }
    }
}
