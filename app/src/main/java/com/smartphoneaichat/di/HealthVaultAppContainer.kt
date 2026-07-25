package com.smartphoneaichat.di

import android.app.Application
import com.smartphoneaichat.data.persistence.EncryptedHealthRecordRepository
import com.smartphoneaichat.data.persistence.LocalEncryptedDocumentStore
import com.smartphoneaichat.data.persistence.PrototypeVaultBackupPolicy
import com.smartphoneaichat.data.persistence.VaultStorageCoordinator
import com.smartphoneaichat.data.governance.InMemoryAuditRepository
import com.smartphoneaichat.data.governance.InMemoryConsentRepository
import com.smartphoneaichat.data.governance.InMemoryProfileRepository
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
import com.smartphoneaichat.domain.repository.EncryptedDocumentStore
import com.smartphoneaichat.domain.repository.HealthRecordRepository
import com.smartphoneaichat.domain.repository.VaultCipher
import com.smartphoneaichat.domain.repository.VaultBackupPolicy
import com.smartphoneaichat.domain.repository.VaultKeyManager
import com.smartphoneaichat.domain.repository.VaultSession
import com.smartphoneaichat.domain.repository.AuditRepository
import com.smartphoneaichat.domain.repository.ConsentRepository
import com.smartphoneaichat.domain.repository.ProfileRepository
import com.smartphoneaichat.domain.session.ProfileSessionCoordinator
import com.smartphoneaichat.domain.usecase.DefaultSelfProfileInitializer
import com.smartphoneaichat.domain.usecase.SelfProfileInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Application-lifetime dependencies used by the active Health Vault product. */
class HealthVaultAppContainer(application: Application) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val randomBytes = AndroidSecureRandomBytes()
    private val defaultVaultSession = DefaultVaultSession()
    val vaultSession: VaultSession = defaultVaultSession
    val profileRepository: ProfileRepository = InMemoryProfileRepository()
    val consentRepository: ConsentRepository = InMemoryConsentRepository()
    val auditRepository: AuditRepository = InMemoryAuditRepository()
    val selfProfileInitializer: SelfProfileInitializer = DefaultSelfProfileInitializer(profileRepository)
    val profileSessionCoordinator = ProfileSessionCoordinator(
        initialContext = null,
        invalidators = emptyList(),
    )
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
    private val vaultStorageRoot = application.filesDir.resolve("vault").toPath()
    val healthRecordRepository: HealthRecordRepository = EncryptedHealthRecordRepository(
        rootDirectory = vaultStorageRoot,
        cipher = vaultCipher,
    )
    val encryptedDocumentStore: EncryptedDocumentStore = LocalEncryptedDocumentStore(
        rootDirectory = vaultStorageRoot,
        cipher = vaultCipher,
    )
    val vaultStorageCoordinator: VaultStorageCoordinator = VaultStorageCoordinator(
        records = healthRecordRepository,
        documents = encryptedDocumentStore,
    )
    val vaultBackupPolicy: VaultBackupPolicy = PrototypeVaultBackupPolicy
    val appSessionStore: AppSessionStore = DefaultAppSessionStore(
        onboardingStatusStorage = SharedPreferencesOnboardingStatusStorage(application),
        vaultSession = vaultSession,
        scope = applicationScope,
    )
}
