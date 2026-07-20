package com.smartphoneaichat.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.repository.AppSessionStore
import com.smartphoneaichat.domain.repository.VaultKeyManager
import com.smartphoneaichat.domain.repository.VaultSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class VaultAccessError {
    InvalidCredentials,
    AlreadyExists,
    KeyInvalidated,
    Unavailable,
}

class VaultAccessViewModelFactory(
    private val keyManager: VaultKeyManager,
    private val vaultSession: VaultSession,
    private val appSessionStore: AppSessionStore,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(VaultAccessViewModel::class.java))
        return VaultAccessViewModel(keyManager, vaultSession, appSessionStore) as T
    }
}

data class VaultAccessUiState(
    val username: String = "",
    val password: String = "",
    val isWorking: Boolean = false,
    val error: VaultAccessError? = null,
) {
    val canSubmit: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isWorking
}

/** Owns credential input and delegates every access decision to the cryptographic boundary. */
class VaultAccessViewModel(
    private val keyManager: VaultKeyManager,
    private val vaultSession: VaultSession,
    private val appSessionStore: AppSessionStore,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _state = MutableStateFlow(VaultAccessUiState())
    val state: StateFlow<VaultAccessUiState> = _state.asStateFlow()

    fun onUsernameChanged(username: String) {
        _state.update { it.copy(username = username, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _state.update { it.copy(password = password, error = null) }
    }

    fun createVault() = performAccess(isProvisioning = true)

    fun unlockVault() = performAccess(isProvisioning = false)

    fun lockVault() {
        vaultSession.lock()
        _state.update { it.copy(password = "", error = null) }
    }

    private fun performAccess(isProvisioning: Boolean) {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(isWorking = true, error = null) }
        viewModelScope.launch {
            val result = withContext(workerDispatcher) {
                val password = current.password.toCharArray()
                try {
                    runCatching {
                        if (isProvisioning) {
                            keyManager.createVault(current.username, password)
                        } else {
                            keyManager.unlock(current.username, password)
                        }
                    }.getOrElse { VaultAccessResult.Unavailable }
                } finally {
                    password.fill('\u0000')
                }
            }
            if (result == VaultAccessResult.Success && isProvisioning) {
                appSessionStore.completeOnboarding()
            }
            _state.update {
                it.copy(
                    password = "",
                    isWorking = false,
                    error = result.toUiError(),
                )
            }
        }
    }

    private fun VaultAccessResult.toUiError(): VaultAccessError? = when (this) {
        VaultAccessResult.Success -> null
        VaultAccessResult.InvalidCredentials -> VaultAccessError.InvalidCredentials
        VaultAccessResult.AlreadyExists -> VaultAccessError.AlreadyExists
        VaultAccessResult.KeyInvalidated -> VaultAccessError.KeyInvalidated
        VaultAccessResult.Unavailable -> VaultAccessError.Unavailable
    }
}
