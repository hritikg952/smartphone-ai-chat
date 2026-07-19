package com.smartphoneaichat.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.smartphoneaichat.presentation.onboarding.OnboardingStep
import com.smartphoneaichat.presentation.onboarding.OnboardingUiState
import com.smartphoneaichat.presentation.session.AppDestination
import com.smartphoneaichat.presentation.session.AppDestinationResolver
import com.smartphoneaichat.presentation.session.AppSessionState
import com.smartphoneaichat.ui.screens.OnboardingScreen
import com.smartphoneaichat.ui.screens.CredentialsSetupScreen
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.SmartphoneAIChatTheme

/** Root Compose seam for application-level destination selection. */
@Composable
fun PersonalHealthVaultApp(
    sessionState: AppSessionState,
    modifier: Modifier = Modifier,
    onboardingState: OnboardingUiState = OnboardingUiState(),
    onGetStarted: () -> Unit = {},
) {
    SmartphoneAIChatTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = DarkBackground,
        ) {
            when (AppDestinationResolver.resolve(sessionState)) {
                AppDestination.Onboarding -> when (onboardingState.step) {
                    OnboardingStep.Welcome -> OnboardingScreen(
                        onGetStarted = onGetStarted,
                    )

                    OnboardingStep.Credentials -> CredentialsSetupScreen()
                }

                AppDestination.Unlock,
                AppDestination.Home,
                -> Unit
            }
        }
    }
}
