package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.EmergencyCardIntegritySigner
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.EmergencyCardProjection
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.repository.EmergencyCardRepository
import com.smartphoneaichat.domain.repository.EmergencyCardReadResult
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.Base64

/** File-backed, authenticated store for the intentionally readable emergency-card snapshot. */
class LocalEmergencyCardRepository(
    private val rootDirectory: Path,
    private val signer: EmergencyCardIntegritySigner,
) : EmergencyCardRepository {
    private val projectionFile = rootDirectory.resolve(FILE_NAME)

    override fun publicCard(): EmergencyCardReadResult {
        if (!Files.exists(projectionFile)) return EmergencyCardReadResult.NotPublished
        return runCatching {
            val lines = Files.readAllLines(projectionFile, StandardCharsets.UTF_8)
            require(lines.size == 7 && lines.first() == FILE_VERSION)
            val payload = lines.take(6).joinToString(separator = "\n").encodeToByteArray()
            val signature = Base64.getDecoder().decode(lines[6])
            if (!signer.verify(payload, signature)) return@runCatching EmergencyCardReadResult.Unavailable
            EmergencyCardReadResult.Available(EmergencyCardProjection(
                profileId = decode(lines[1]),
                preferredName = decode(lines[2]),
                schemaVersion = lines[3].toInt(),
                publishedAtEpochMillis = lines[4].toLong(),
                lastRefreshedAtEpochMillis = lines[5].toLong(),
            ))
        }.getOrDefault(EmergencyCardReadResult.Unavailable)
    }

    override fun publish(context: AuthorizedSessionContext, projection: EmergencyCardProjection) {
        context.requireAccess(projection.profileId, ProfileCapability.ManageEmergencyProjection)
        val lines = listOf(
            FILE_VERSION,
            encode(projection.profileId),
            encode(projection.preferredName),
            projection.schemaVersion.toString(),
            projection.publishedAtEpochMillis.toString(),
            projection.lastRefreshedAtEpochMillis.toString(),
        )
        val payload = lines.joinToString(separator = "\n").encodeToByteArray()
        val serialized = lines.plus(Base64.getEncoder().encodeToString(signer.sign(payload)))
            .joinToString(separator = "\n", postfix = "\n")
        writeAtomically(serialized)
    }

    override fun revoke(context: AuthorizedSessionContext) {
        context.requireAccess(context.profileId, ProfileCapability.ManageEmergencyProjection)
        Files.deleteIfExists(projectionFile)
    }

    private fun writeAtomically(contents: String) {
        Files.createDirectories(rootDirectory)
        val temporaryFile = Files.createTempFile(rootDirectory, "emergency-card", ".tmp")
        try {
            Files.write(temporaryFile, contents.toByteArray(StandardCharsets.UTF_8))
            runCatching {
                Files.move(temporaryFile, projectionFile, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse {
                Files.move(temporaryFile, projectionFile, StandardCopyOption.REPLACE_EXISTING)
            }
        } finally {
            Files.deleteIfExists(temporaryFile)
        }
    }

    private fun encode(value: String): String = Base64.getEncoder().encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    private fun decode(value: String): String = String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8)

    private companion object {
        const val FILE_NAME = "emergency-card.v1"
        const val FILE_VERSION = "1"
    }
}
