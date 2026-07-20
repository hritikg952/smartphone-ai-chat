package com.smartphoneaichat.data.session

import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.domain.model.AppSessionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAppSessionStoreTest {

    @Test
    fun legacyOnboardingFlagWithoutVaultEnvelope_returnsToOnboarding() = runTest {
        val store = DefaultAppSessionStore(
            onboardingStatusStorage = FakeOnboardingStatusStorage(hasCompletedOnboarding = true),
            vaultSession = DefaultVaultSession(),
            scope = backgroundScope,
        )

        runCurrent()

        assertEquals(
            AppSessionState(hasCompletedOnboarding = false, isVaultUnlocked = false),
            store.state.value,
        )
    }

    @Test
    fun onboardingPersistsButUnlockStateComesOnlyFromVaultSession() = runTest {
        val storage = FakeOnboardingStatusStorage()
        val vaultSession = DefaultVaultSession()
        val firstProcess = DefaultAppSessionStore(storage, vaultSession, backgroundScope)

        vaultSession.unlock(ByteArray(32) { 4 })
        firstProcess.completeOnboarding()
        runCurrent()

        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = true),
            firstProcess.state.value,
        )

        vaultSession.lock()
        runCurrent()
        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = false),
            firstProcess.state.value,
        )

        val restartedVaultSession = DefaultVaultSession().apply { markVaultPresent() }
        val restartedProcess = DefaultAppSessionStore(
            storage,
            restartedVaultSession,
            backgroundScope,
        )
        runCurrent()
        assertEquals(
            AppSessionState(hasCompletedOnboarding = true, isVaultUnlocked = false),
            restartedProcess.state.value,
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
