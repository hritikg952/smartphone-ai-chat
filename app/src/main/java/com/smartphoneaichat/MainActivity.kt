package com.smartphoneaichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartphoneaichat.presentation.onboarding.OnboardingViewModel
import com.smartphoneaichat.presentation.session.AppSessionState
import com.smartphoneaichat.ui.app.PersonalHealthVaultApp

/** Single Android entry point for the Personal Health Vault application. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val onboardingViewModel: OnboardingViewModel = viewModel()
            val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
            val sessionState = remember {
                AppSessionState(
                    hasCompletedOnboarding = false,
                    isVaultUnlocked = false,
                )
            }
            PersonalHealthVaultApp(
                sessionState = sessionState,
                onboardingState = onboardingState,
                onGetStarted = onboardingViewModel::onGetStarted,
            )
        }
    }
}
