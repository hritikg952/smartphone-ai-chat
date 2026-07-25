package com.smartphoneaichat.ui.screens

import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.presentation.emergency.EmergencyCardUiState
import org.junit.Rule
import org.junit.Test

class EmergencyCardScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun lockedCard_showsOnlyPublishedSnapshotAndNoVaultManagementControls() {
        composeRule.setContent {
            EmergencyCardScreen(
                state = EmergencyCardUiState(
                    projection = EmergencyCardProjection("self", "Avery", 1, 10, 10),
                ),
                isVaultUnlocked = false,
                onRequestPublish = {},
                onDismissExposureWarning = {},
                onConfirmPublish = {},
                onRevoke = {},
            )
        }

        composeRule.onNodeWithText("Avery").assertIsDisplayed()
        composeRule.onNodeWithText("Disable emergency card").assertDoesNotExist()
        composeRule.onNodeWithText("This is user-selected reference information. Call local emergency services and seek professional care.")
            .assertIsDisplayed()
    }

    @Test
    fun unlockedUnpublishedCard_requiresExposureConfirmationBeforePublishing() {
        composeRule.setContent {
            EmergencyCardScreen(
                state = EmergencyCardUiState(currentPreferredName = "Avery", showExposureWarning = true),
                isVaultUnlocked = true,
                onRequestPublish = {},
                onDismissExposureWarning = {},
                onConfirmPublish = {},
                onRevoke = {},
            )
        }

        composeRule.onNodeWithText("Share emergency information?").assertIsDisplayed()
        composeRule.onNodeWithText("Publish name").assertIsDisplayed()
    }
}
