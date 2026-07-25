package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.data.security.AesGcmVaultCipher
import com.smartphoneaichat.data.security.DefaultVaultSession
import com.smartphoneaichat.data.security.RandomBytes
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.DailyMedicationSchedule
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.MedicationStatus
import com.smartphoneaichat.domain.model.ProfileCapability
import com.smartphoneaichat.domain.model.ProfileRole
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime

class EncryptedMedicationRepositoryTest {
    @TempDir lateinit var directory: Path

    @Test
    fun save_replacesTheCurrentRegimenAndKeepsItProfileScoped() {
        val session = DefaultVaultSession().also { it.unlock(ByteArray(32) { 7 }) }
        val repository = EncryptedMedicationRepository(
            EncryptedHealthRecordRepository(directory, AesGcmVaultCipher(session, FixedRandomBytes())),
        )
        val first = regimen(label = "Vitamin D", profileId = "self", updatedAt = 10)
        val replacement = first.copy(label = "Vitamin D3", updatedAtEpochMillis = 20)

        assertEquals(HealthRecordSaveResult.Saved, repository.save(context("self"), first))
        assertEquals(HealthRecordSaveResult.Saved, repository.save(context("self"), replacement))

        assertEquals(listOf("Vitamin D3"), repository.list(context("self")).map { it.label })
        assertEquals(emptyList<MedicationRegimen>(), repository.list(context("other")))
    }

    private fun regimen(label: String, profileId: String, updatedAt: Long) = MedicationRegimen(
        id = "med-1", profileId = profileId, label = label, doseAmount = "1", doseUnit = "tablet",
        route = "Oral", form = "Tablet", startDate = LocalDate.of(2026, 7, 1), status = MedicationStatus.Active,
        source = "Manual entry", schedule = DailyMedicationSchedule(listOf(LocalTime.of(8, 0))),
        createdAtEpochMillis = 10, updatedAtEpochMillis = updatedAt,
    )

    private fun context(profileId: String) = AuthorizedSessionContext(
        actorId = "owner", profileId = profileId, sessionId = "session-$profileId", role = ProfileRole.Self,
        capabilities = ProfileCapability.entries.toSet(),
    )

    private class FixedRandomBytes : RandomBytes {
        override fun next(size: Int) = ByteArray(size) { 3 }
    }
}
