package com.smartphoneaichat.data.session

import com.smartphoneaichat.domain.model.AppSessionState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultAppSessionStoreTest {

    @Test
    fun completingOnboardingPersistsCompletionButUnlockRemainsProcessLocal() {
        val storage = FakeOnboardingStatusStorage()
        val firstProcess = DefaultAppSessionStore(storage)

        firstProcess.completeOnboarding()

        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = true),
            firstProcess.state.value,
        )

        val restartedProcess = DefaultAppSessionStore(storage)
        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = false),
            restartedProcess.state.value,
        )
    }

    @Test
    fun completedSessionCanBeLockedAndUnlockedWithoutChangingOnboardingStatus() {
        val store = DefaultAppSessionStore(
            FakeOnboardingStatusStorage(hasCompletedOnboarding = true),
        )

        store.unlockVault()
        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = true),
            store.state.value,
        )

        store.lockVault()
        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = false),
            store.state.value,
        )
    }

    private class FakeOnboardingStatusStorage(
        private var hasCompletedOnboarding: Boolean = false,
    ) : OnboardingStatusStorage {

        override fun readHasCompletedOnboarding(): Boolean = hasCompletedOnboarding

        override fun writeHasCompletedOnboarding(value: Boolean) {
            hasCompletedOnboarding = value
        }
    }
}
