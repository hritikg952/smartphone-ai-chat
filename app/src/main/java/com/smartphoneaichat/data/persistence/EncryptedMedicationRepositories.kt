package com.smartphoneaichat.data.persistence

import com.smartphoneaichat.domain.model.AsNeededMedicationSchedule
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.DailyMedicationSchedule
import com.smartphoneaichat.domain.model.HealthRecordProvenance
import com.smartphoneaichat.domain.model.HealthRecordWrite
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.MedicationSchedule
import com.smartphoneaichat.domain.model.MedicationStatus
import com.smartphoneaichat.domain.model.Provider
import com.smartphoneaichat.domain.model.UnsupportedMedicationSchedule
import com.smartphoneaichat.domain.model.WeeklyMedicationSchedule
import com.smartphoneaichat.domain.repository.HealthRecordDeleteResult
import com.smartphoneaichat.domain.repository.HealthRecordRepository
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import com.smartphoneaichat.domain.repository.MedicationRepository
import com.smartphoneaichat.domain.repository.ProviderRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.Base64

/** Feature repositories keep medication-specific serialization outside the generic encrypted store. */
class EncryptedMedicationRepository(
    private val records: HealthRecordRepository,
) : MedicationRepository {
    override fun save(context: AuthorizedSessionContext, regimen: MedicationRegimen): HealthRecordSaveResult =
        records.save(context, regimen.toRecord())

    override fun get(context: AuthorizedSessionContext, id: String): MedicationRegimen? =
        records.get(context, id)?.takeIf { it.type == TYPE }?.let(MedicationRecordCodec::decodeRegimen)

    override fun list(context: AuthorizedSessionContext): List<MedicationRegimen> =
        records.list(context, limit = MAX_RECORDS).asSequence()
            .filter { it.type == TYPE }
            .mapNotNull(MedicationRecordCodec::decodeRegimen)
            .sortedWith(compareBy<MedicationRegimen> { it.status != MedicationStatus.Active }.thenBy { it.label.lowercase() })
            .toList()

    override fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult = records.delete(context, id)

    private fun MedicationRegimen.toRecord() = HealthRecordWrite(
        id = id, profileId = profileId, type = TYPE, schemaVersion = SCHEMA_VERSION,
        createdAtEpochMillis = createdAtEpochMillis, updatedAtEpochMillis = updatedAtEpochMillis,
        provenance = HealthRecordProvenance.ManualEntry, plaintext = MedicationRecordCodec.encode(this),
    )

    private companion object {
        const val TYPE = "medication-regimen"
        const val SCHEMA_VERSION = 1
        const val MAX_RECORDS = 1_000
    }
}

class EncryptedProviderRepository(
    private val records: HealthRecordRepository,
) : ProviderRepository {
    override fun save(context: AuthorizedSessionContext, provider: Provider): HealthRecordSaveResult =
        records.save(context, provider.toRecord())

    override fun get(context: AuthorizedSessionContext, id: String): Provider? =
        records.get(context, id)?.takeIf { it.type == TYPE }?.let(MedicationRecordCodec::decodeProvider)

    override fun list(context: AuthorizedSessionContext): List<Provider> =
        records.list(context, limit = MAX_RECORDS).asSequence()
            .filter { it.type == TYPE }
            .mapNotNull(MedicationRecordCodec::decodeProvider)
            .sortedBy { it.name.lowercase() }
            .toList()

    override fun delete(context: AuthorizedSessionContext, id: String): HealthRecordDeleteResult = records.delete(context, id)

    private fun Provider.toRecord() = HealthRecordWrite(
        id = id, profileId = profileId, type = TYPE, schemaVersion = SCHEMA_VERSION,
        createdAtEpochMillis = createdAtEpochMillis, updatedAtEpochMillis = updatedAtEpochMillis,
        provenance = HealthRecordProvenance.ManualEntry, plaintext = MedicationRecordCodec.encode(this),
    )

    private companion object {
        const val TYPE = "provider"
        const val SCHEMA_VERSION = 1
        const val MAX_RECORDS = 1_000
    }
}

private object MedicationRecordCodec {
    fun encode(regimen: MedicationRegimen): ByteArray = fields(
        "medication", regimen.id, regimen.profileId, regimen.label, regimen.indication.orEmpty(), regimen.doseAmount,
        regimen.doseUnit, regimen.route, regimen.form, regimen.startDate.toString(), regimen.endDate?.toString().orEmpty(),
        regimen.status.name, regimen.providerId.orEmpty(), regimen.source, regimen.notes, scheduleKind(regimen.schedule),
        regimen.schedule.originalInstruction, scheduleTimes(regimen.schedule), scheduleDays(regimen.schedule),
        regimen.createdAtEpochMillis.toString(), regimen.updatedAtEpochMillis.toString(),
    )

    fun decodeRegimen(bytes: ByteArray): MedicationRegimen? = runCatching {
        val p = read(bytes)
        require(p.size == 21 && p[0] == "medication")
        MedicationRegimen(
            id = p[1], profileId = p[2], label = p[3], indication = p[4].ifBlank { null }, doseAmount = p[5], doseUnit = p[6],
            route = p[7], form = p[8], startDate = LocalDate.parse(p[9]), endDate = p[10].ifBlank { null }?.let(LocalDate::parse),
            status = MedicationStatus.valueOf(p[11]), providerId = p[12].ifBlank { null }, source = p[13], notes = p[14],
            schedule = schedule(p[15], p[16], p[17], p[18]), createdAtEpochMillis = p[19].toLong(), updatedAtEpochMillis = p[20].toLong(),
        )
    }.getOrNull()

    fun encode(provider: Provider): ByteArray = fields(
        "provider", provider.id, provider.profileId, provider.name, provider.specialty, provider.facility,
        provider.contact, provider.createdAtEpochMillis.toString(), provider.updatedAtEpochMillis.toString(),
    )

    fun decodeProvider(bytes: ByteArray): Provider? = runCatching {
        val p = read(bytes)
        require(p.size == 9 && p[0] == "provider")
        Provider(p[1], p[2], p[3], p[4], p[5], p[6], p[7].toLong(), p[8].toLong())
    }.getOrNull()

    private fun fields(vararg values: String): ByteArray = values.joinToString("\t") { it.b64() }.encodeToByteArray()
    private fun read(bytes: ByteArray): List<String> = bytes.decodeToString().split("\t").map(String::unb64)
    private fun String.b64(): String = Base64.getUrlEncoder().withoutPadding().encodeToString(encodeToByteArray())
    private fun String.unb64(): String = Base64.getUrlDecoder().decode(this).decodeToString()
    private fun scheduleKind(schedule: MedicationSchedule) = when (schedule) {
        is AsNeededMedicationSchedule -> "as-needed"
        is DailyMedicationSchedule -> "daily"
        is WeeklyMedicationSchedule -> "weekly"
        is UnsupportedMedicationSchedule -> "unsupported"
    }
    private fun scheduleTimes(schedule: MedicationSchedule) = when (schedule) {
        is DailyMedicationSchedule -> schedule.times.joinToString(",")
        is WeeklyMedicationSchedule -> schedule.times.joinToString(",")
        else -> ""
    }
    private fun scheduleDays(schedule: MedicationSchedule) = (schedule as? WeeklyMedicationSchedule)
        ?.days?.joinToString(",") { it.name }.orEmpty()
    private fun schedule(kind: String, instruction: String, times: String, days: String): MedicationSchedule = when (kind) {
        "as-needed" -> AsNeededMedicationSchedule(instruction)
        "daily" -> DailyMedicationSchedule(times.split(",").map(LocalTime::parse), instruction)
        "weekly" -> WeeklyMedicationSchedule(days.split(",").map(DayOfWeek::valueOf).toSet(), times.split(",").map(LocalTime::parse), instruction)
        "unsupported" -> UnsupportedMedicationSchedule(instruction)
        else -> error("Unknown medication schedule")
    }
}
