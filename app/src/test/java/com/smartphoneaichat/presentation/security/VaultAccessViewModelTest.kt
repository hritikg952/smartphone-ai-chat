package com.smartphoneaichat.presentation.security

import com.smartphoneaichat.domain.model.AppSessionState
import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.repository.AppSessionStore
import com.smartphoneaichat.domain.repository.VaultKeyManager
import com.smartphoneaichat.domain.repository.VaultSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VaultAccessViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createVault_successCompletesOnboardingAndClearsPassword() = runTest(testDispatcher) {
        val keyManager = FakeVaultKeyManager(createResult = VaultAccessResult.Success)
        val appSessionStore = FakeAppSessionStore()
        val viewModel = VaultAccessViewModel(
            keyManager = keyManager,
            vaultSession = FakeVaultSession(),
            appSessionStore = appSessionStore,
            workerDispatcher = testDispatcher,
        )
        viewModel.onUsernameChanged("owner")
        viewModel.onPasswordChanged("vault-pass")

        viewModel.createVault()
        advanceUntilIdle()

        assertEquals("owner", keyManager.createdUsername)
        assertEquals("vault-pass", keyManager.createdPassword)
        assertEquals(true, appSessionStore.didCompleteOnboarding)
        assertEquals("", viewModel.state.value.password)
        assertEquals(null, viewModel.state.value.error)
    }

    @Test
    fun unlockVault_failureClearsPasswordAndExposesTypedError() = runTest(testDispatcher) {
        val viewModel = VaultAccessViewModel(
            keyManager = FakeVaultKeyManager(
                createResult = VaultAccessResult.Success,
                unlockResult = VaultAccessResult.InvalidCredentials,
            ),
            vaultSession = FakeVaultSession(),
            appSessionStore = FakeAppSessionStore(),
            workerDispatcher = testDispatcher,
        )
        viewModel.onUsernameChanged("owner")
        viewModel.onPasswordChanged("wrong-pass")

        viewModel.unlockVault()
        advanceUntilIdle()

        assertEquals("", viewModel.state.value.password)
        assertEquals(VaultAccessError.InvalidCredentials, viewModel.state.value.error)
    }

    @Test
    fun accessInfrastructureFailure_clearsWorkingStateAndReturnsUnavailable() =
        runTest(testDispatcher) {
            val viewModel = VaultAccessViewModel(
                keyManager = ThrowingVaultKeyManager(),
                vaultSession = FakeVaultSession(),
                appSessionStore = FakeAppSessionStore(),
                workerDispatcher = testDispatcher,
            )
            viewModel.onUsernameChanged("owner")
            viewModel.onPasswordChanged("vault-pass")

            viewModel.unlockVault()
            advanceUntilIdle()

            assertEquals(false, viewModel.state.value.isWorking)
            assertEquals("", viewModel.state.value.password)
            assertEquals(VaultAccessError.Unavailable, viewModel.state.value.error)
        }

    private class FakeVaultKeyManager(
        private val createResult: VaultAccessResult,
        private val unlockResult: VaultAccessResult = VaultAccessResult.Unavailable,
    ) : VaultKeyManager {
        var createdUsername: String? = null
        var createdPassword: String? = null

        override fun createVault(username: String, password: CharArray): VaultAccessResult {
            createdUsername = username
            createdPassword = password.concatToString()
            return createResult
        }

        override fun unlock(username: String, password: CharArray): VaultAccessResult =
            unlockResult

        override fun destroy() = Unit
    }

    private class FakeVaultSession : VaultSession {
        override val state = MutableStateFlow(com.smartphoneaichat.domain.model.VaultSessionState.Absent)
        override fun lock() = Unit
    }

    private class ThrowingVaultKeyManager : VaultKeyManager {
        override fun createVault(username: String, password: CharArray): VaultAccessResult =
            error("storage unavailable")

        override fun unlock(username: String, password: CharArray): VaultAccessResult =
            error("keystore unavailable")

        override fun destroy() = Unit
    }

    private class FakeAppSessionStore : AppSessionStore {
        override val state: StateFlow<AppSessionState> = MutableStateFlow(
            AppSessionState(hasCompletedOnboarding = false, isVaultUnlocked = false),
        )
        var didCompleteOnboarding = false

        override fun completeOnboarding() {
            didCompleteOnboarding = true
        }

    }
}
