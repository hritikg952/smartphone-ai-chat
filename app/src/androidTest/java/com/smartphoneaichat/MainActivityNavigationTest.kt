package com.smartphoneaichat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun activityRecreationRestoresProtectedRouteWhileSessionRemainsUnlocked() {
        composeRule.runOnUiThread {
            (composeRule.activity.application as App)
                .healthVaultContainer.appSessionStore.completeOnboarding()
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Medications").performScrollTo().performClick()
        composeRule.onNodeWithText("Medications").assertIsDisplayed()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText("Medications").assertIsDisplayed()
    }
}
