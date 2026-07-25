package com.smartphoneaichat.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

enum class MedicationStatus { Active, Paused, Discontinued }

sealed interface MedicationSchedule {
    val originalInstruction: String
}

data class AsNeededMedicationSchedule(
    override val originalInstruction: String,
) : MedicationSchedule {
    init { require(originalInstruction.isNotBlank()) }
}

data class DailyMedicationSchedule(
    val times: List<LocalTime>,
    override val originalInstruction: String = "",
) : MedicationSchedule {
    init { require(times.isNotEmpty() && times.distinct().size == times.size) }
}

data class WeeklyMedicationSchedule(
    val days: Set<DayOfWeek>,
    val times: List<LocalTime>,
    override val originalInstruction: String = "",
) : MedicationSchedule {
    init {
        require(days.isNotEmpty())
        require(times.isNotEmpty() && times.distinct().size == times.size)
    }
}

/** Visible-only instruction for schedules this prototype does not materialize. */
data class UnsupportedMedicationSchedule(
    override val originalInstruction: String,
) : MedicationSchedule {
    init { require(originalInstruction.isNotBlank()) }
}

data class MedicationRegimen(
    val id: String,
    val profileId: String,
    val label: String,
    val indication: String? = null,
    val doseAmount: String = "1",
    val doseUnit: String = "dose",
    val route: String,
    val form: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val status: MedicationStatus,
    val providerId: String? = null,
    val source: String,
    val notes: String = "",
    val schedule: MedicationSchedule,
    val createdAtEpochMillis: Long = 0,
    val updatedAtEpochMillis: Long = 0,
) {
    init {
        require(id.isNotBlank() && profileId.isNotBlank() && label.isNotBlank())
        require(doseAmount.isNotBlank() && doseUnit.isNotBlank())
        require(route.isNotBlank() && form.isNotBlank() && source.isNotBlank())
        require(endDate == null || !endDate.isBefore(startDate))
        require(createdAtEpochMillis >= 0 && updatedAtEpochMillis >= createdAtEpochMillis)
    }
}

data class Provider(
    val id: String,
    val profileId: String,
    val name: String,
    val specialty: String = "",
    val facility: String = "",
    val contact: String = "",
    val createdAtEpochMillis: Long = 0,
    val updatedAtEpochMillis: Long = 0,
) {
    init {
        require(id.isNotBlank() && profileId.isNotBlank() && name.isNotBlank())
        require(createdAtEpochMillis >= 0 && updatedAtEpochMillis >= createdAtEpochMillis)
    }
}
