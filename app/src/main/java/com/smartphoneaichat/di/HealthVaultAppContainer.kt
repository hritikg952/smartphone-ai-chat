package com.smartphoneaichat.di

import android.app.Application
import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.data.security.AesGcmVaultCipher
import com.smartphoneaichat.data.security.AndroidKeystoreVaultKeyEnvelopeCipher
import com.smartphoneaichat.data.security.AndroidSecureRandomBytes
import com.smartphoneaichat.data.security.DefaultVaultKeyManager
import com.smartphoneaichat.data.security.Pbkdf2AuthenticationGateway
import com.smartphoneaichat.data.security.SharedPreferencesVaultSecurityStorage
import com.smartphoneaichat.data.session.DefaultAppSessionStore
import com.smartphoneaichat.data.session.SharedPreferencesOnboardingStatusStorage
import com.smartphoneaichat.domain.repository.AppSessionStore
import com.smartphoneaichat.domain.repository.VaultCipher
import com.smartphoneaichat.domain.repository.VaultKeyManager
import com.smartphoneaichat.domain.repository.VaultSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Application-lifetime dependencies used by the active Health Vault product. */
class HealthVaultAppContainer(application: Application) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val randomBytes = AndroidSecureRandomBytes()
    private val defaultVaultSession = DefaultVaultSession()
    val vaultSession: VaultSession = defaultVaultSession
    val vaultKeyManager: VaultKeyManager = DefaultVaultKeyManager(
        storage = SharedPreferencesVaultSecurityStorage(application),
        authenticationGateway = Pbkdf2AuthenticationGateway(randomBytes),
        keyEnvelopeCipher = AndroidKeystoreVaultKeyEnvelopeCipher(),
        randomBytes = randomBytes,
        session = defaultVaultSession,
    )
    val vaultCipher: VaultCipher = AesGcmVaultCipher(
        session = defaultVaultSession,
        randomBytes = randomBytes,
    )
    val appSessionStore: AppSessionStore = DefaultAppSessionStore(
        onboardingStatusStorage = SharedPreferencesOnboardingStatusStorage(application),
        vaultSession = vaultSession,
        scope = applicationScope,
    )
}
