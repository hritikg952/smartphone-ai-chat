package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.model.HealthRecord
import com.smartphoneaichat.domain.model.HealthRecordProvenance
import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.VaultAssociatedData
import com.smartphoneaichat.domain.model.VaultCryptoResult
import com.smartphoneaichat.domain.model.VaultEncryptedPayload
import com.smartphoneaichat.domain.repository.HealthRecordRepository
import com.smartphoneaichat.domain.repository.HealthRecordDeleteResult
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import com.smartphoneaichat.domain.repository.VaultCipher
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64

class EncryptedHealthRecordRepository(
    private val rootDirectory: Path,
    private val cipher: VaultCipher,
) : HealthRecordRepository {
    private val recordsFile: Path = rootDirectory.resolve(FILE_NAME)

    override fun save(context: AuthorizedSessionContext, record: HealthRecordWrite): HealthRecordSaveResult {
        context.requireAccess(record.profileId, ProfileCapability.Write)
        val encrypted = cipher.encrypt(record.plaintext, record.associatedData())
        if (encrypted == VaultCryptoResult.Locked) return HealthRecordSaveResult.Locked
        if (encrypted !is VaultCryptoResult.Encrypted) return HealthRecordSaveResult.Unavailable

        return if (runCatching { writeRecords(
                loadStoredRecords()
                    .filterNot { it.profileId == record.profileId && it.id == record.id }
                    .plus(record.toStoredRecord(encrypted.payload)),
            )
        }.getOrDefault(false)) {
            HealthRecordSaveResult.Saved
        } else {
            HealthRecordSaveResult.Unavailable
        }
    }

    override fun get(context: AuthorizedSessionContext, id: String): HealthRecord? {
        context.requireAccess(context.profileId, ProfileCapability.Read)
        val stored = runCatching { loadStoredRecords() }.getOrDefault(emptyList()).firstOrNull {
            it.profileId == context.profileId && it.id == id
        } ?: return null
        return stored.decrypt()
    }

    override fun list(context: AuthorizedSessionContext, limit: Int, offset: Int): List<HealthRecord> {
        context.requireAccess(context.profileId, ProfileCapability.Read)
        require(limit > 0)
        require(offset >= 0)
        return runCatching {
            loadStoredRecords()
                .filter { it.profileId == context.profileId }
                .sortedWith(compareByDescending<StoredHealthRecord> { it.updatedAtEpochMillis }.thenBy { it.id })
                .drop(offset)
                .take(limit)
                .mapNotNull { it.decrypt() }
        }.getOrDefault(emptyList())
    }

    override fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult {
        context.requireAccess(context.profileId, ProfileCapability.Delete)
        return runCatching {
            val stored = loadStoredRecords()
            val updated = stored.filterNot { it.profileId == context.profileId && it.id == id }
            if (updated.size == stored.size) return HealthRecordDeleteResult.NotFound
            if (writeRecords(updated)) {
                HealthRecordDeleteResult.Deleted
            } else {
                HealthRecordDeleteResult.Unavailable
            }
        }.getOrDefault(HealthRecordDeleteResult.Unavailable)
    }

    private fun StoredHealthRecord.decrypt(): HealthRecord? {
        return when (val decrypted = cipher.decrypt(payload, associatedData())) {
            is VaultCryptoResult.Plaintext -> HealthRecord(
                id = id,
                profileId = profileId,
                type = type,
                schemaVersion = schemaVersion,
                createdAtEpochMillis = createdAtEpochMillis,
                updatedAtEpochMillis = updatedAtEpochMillis,
                provenance = provenance,
                plaintext = decrypted.bytes,
            )
            else -> null
        }
    }

    private fun HealthRecordWrite.toStoredRecord(payload: VaultEncryptedPayload): StoredHealthRecord =
        StoredHealthRecord(
            id = id,
            profileId = profileId,
            type = type,
            schemaVersion = schemaVersion,
            createdAtEpochMillis = createdAtEpochMillis,
            updatedAtEpochMillis = updatedAtEpochMillis,
            provenance = provenance,
            payload = payload,
        )

    private fun HealthRecordWrite.associatedData(): VaultAssociatedData =
        VaultAssociatedData(
            profileId = profileId,
            recordId = id,
            schemaVersion = schemaVersion,
        )

    private fun StoredHealthRecord.associatedData(): VaultAssociatedData =
        VaultAssociatedData(
            profileId = profileId,
            recordId = id,
            schemaVersion = schemaVersion,
        )

    private fun loadStoredRecords(): List<StoredHealthRecord> {
        if (!Files.exists(recordsFile)) return emptyList()
        return Files.readAllLines(recordsFile, StandardCharsets.UTF_8)
            .filter { it.isNotBlank() }
            .map { line -> StoredHealthRecord.decode(line) }
    }

    private fun writeRecords(records: List<StoredHealthRecord>): Boolean = runCatching {
        Files.createDirectories(rootDirectory)
        val tempFile = rootDirectory.resolve("$FILE_NAME.tmp")
        val body = records.joinToString(separator = "\n", postfix = "\n") { it.encode() }
        Files.write(tempFile, body.toByteArray(StandardCharsets.UTF_8))
        replaceFile(tempFile, recordsFile)
    }.isSuccess

    private data class StoredHealthRecord(
        val id: String,
        val profileId: String,
        val type: String,
        val schemaVersion: Int,
        val createdAtEpochMillis: Long,
        val updatedAtEpochMillis: Long,
        val provenance: HealthRecordProvenance,
        val payload: VaultEncryptedPayload,
    ) {
        fun encode(): String = listOf(
            CURRENT_RECORD_VERSION.toString(),
            id.b64(),
            profileId.b64(),
            type.b64(),
            schemaVersion.toString(),
            createdAtEpochMillis.toString(),
            updatedAtEpochMillis.toString(),
            provenance.name,
            payload.version.toString(),
            payload.nonceBase64.b64(),
            payload.ciphertextBase64.b64(),
        ).joinToString("\t")

        companion object {
            fun decode(line: String): StoredHealthRecord {
                val parts = line.split("\t")
                require(parts.size == 11)
                require(parts[0].toInt() == CURRENT_RECORD_VERSION)
                return StoredHealthRecord(
                    id = parts[1].unb64(),
                    profileId = parts[2].unb64(),
                    type = parts[3].unb64(),
                    schemaVersion = parts[4].toInt(),
                    createdAtEpochMillis = parts[5].toLong(),
                    updatedAtEpochMillis = parts[6].toLong(),
                    provenance = HealthRecordProvenance.valueOf(parts[7]),
                    payload = VaultEncryptedPayload(
                        version = parts[8].toInt(),
                        nonceBase64 = parts[9].unb64(),
                        ciphertextBase64 = parts[10].unb64(),
                    ),
                )
            }
        }
    }

    private companion object {
        const val FILE_NAME = "health-records.v1"
        const val CURRENT_RECORD_VERSION = 1

        fun String.b64(): String = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(toByteArray(StandardCharsets.UTF_8))

        fun String.unb64(): String = String(
            Base64.getUrlDecoder().decode(this),
            StandardCharsets.UTF_8,
        )
    }
}
