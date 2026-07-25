package com.smartphoneaichat.presentation.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartphoneaichat.domain.model.VaultAccessResult
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.AuditEvent
import com.smartphoneaichat.domain.model.AuditEventType
import com.smartphoneaichat.domain.model.AuditOutcome
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.repository.AppSessionStore
import com.smartphoneaichat.domain.repository.AuditRepository
import com.smartphoneaichat.domain.repository.VaultKeyManager
import com.smartphoneaichat.domain.repository.VaultSession
import com.smartphoneaichat.domain.session.ProfileSessionCoordinator
import com.smartphoneaichat.domain.usecase.SelfProfileInitializer
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
    private val selfProfileInitializer: SelfProfileInitializer? = null,
    private val profileSessionCoordinator: ProfileSessionCoordinator? = null,
    private val auditRepository: AuditRepository? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(VaultAccessViewModel::class.java))
        return VaultAccessViewModel(
            keyManager,
            vaultSession,
            appSessionStore,
            selfProfileInitializer = selfProfileInitializer,
            profileSessionCoordinator = profileSessionCoordinator,
            auditRepository = auditRepository,
        ) as T
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
    private val selfProfileInitializer: SelfProfileInitializer? = null,
    private val profileSessionCoordinator: ProfileSessionCoordinator? = null,
    private val auditRepository: AuditRepository? = null,
    private val nowEpochMillis: () -> Long = { System.currentTimeMillis() },
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
            val profileReady = if (result == VaultAccessResult.Success) {
                runCatching {
                    val profile = selfProfileInitializer?.ensureSelfProfile(
                        displayName = current.username,
                        nowEpochMillis = nowEpochMillis(),
                    )
                    profileSessionCoordinator?.switchTo(
                        AuthorizedSessionContext(
                            actorId = profile?.actorId ?: "vault-owner",
                            profileId = profile?.id ?: "self",
                            sessionId = "vault-session-${nowEpochMillis()}",
                            role = ProfileRole.Self,
                            capabilities = ProfileCapability.entries.toSet(),
                        ),
                    )
                }.isSuccess
            } else {
                false
            }
            val finalResult = if (result == VaultAccessResult.Success && !profileReady) {
                VaultAccessResult.Unavailable
            } else {
                result
            }
            recordAudit(finalResult, isProvisioning)
            if (finalResult == VaultAccessResult.Success && isProvisioning) {
                appSessionStore.completeOnboarding()
            }
            _state.update {
                it.copy(
                    password = "",
                    isWorking = false,
                    error = finalResult.toUiError(),
                )
            }
        }
    }

    private fun recordAudit(result: VaultAccessResult, isProvisioning: Boolean) {
        val repository = auditRepository ?: return
        val successful = result == VaultAccessResult.Success
        repository.append(
            AuditEvent(
                eventId = "vault-access:${nowEpochMillis()}:${result.toAuditCode()}",
                actorId = "vault-owner",
                profileId = null,
                type = if (successful) AuditEventType.KeyEvent else AuditEventType.UnlockFailure,
                outcome = if (successful) AuditOutcome.Success else AuditOutcome.Failure,
                occurredAtEpochMillis = nowEpochMillis(),
                targetType = "vault",
                detailCode = when {
                    successful && isProvisioning -> "created"
                    successful -> "unlocked"
                    result == VaultAccessResult.InvalidCredentials -> "invalid_credentials"
                    result == VaultAccessResult.KeyInvalidated -> "key_invalidated"
                    else -> "unavailable"
                },
            ),
        )
    }

    private fun VaultAccessResult.toAuditCode(): String = when (this) {
        VaultAccessResult.Success -> "success"
        VaultAccessResult.AlreadyExists -> "already_exists"
        VaultAccessResult.InvalidCredentials -> "invalid_credentials"
        VaultAccessResult.KeyInvalidated -> "key_invalidated"
        VaultAccessResult.Unavailable -> "unavailable"
    }

    private fun VaultAccessResult.toUiError(): VaultAccessError? = when (this) {
        VaultAccessResult.Success -> null
        VaultAccessResult.InvalidCredentials -> VaultAccessError.InvalidCredentials
        VaultAccessResult.AlreadyExists -> VaultAccessError.AlreadyExists
        VaultAccessResult.KeyInvalidated -> VaultAccessError.KeyInvalidated
        VaultAccessResult.Unavailable -> VaultAccessError.Unavailable
    }
}
