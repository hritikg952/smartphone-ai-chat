package com.smartphoneaichat.domain.service

import com.smartphoneaichat.domain.model.DailyMedicationSchedule
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.MedicationStatus
import com.smartphoneaichat.domain.model.WeeklyMedicationSchedule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class ScheduledMedicationDose(
    val regimenId: String,
    val label: String,
    val dose: String,
    val route: String,
    val localTime: LocalTime,
    val scheduledAt: ZonedDateTime,
    val source: String,
    val instruction: String,
)

/** Materializes only the schedule forms explicitly supported by the offline prototype. */
class MedicationScheduleMaterializer {
    fun materializeToday(
        regimen: MedicationRegimen,
        date: LocalDate,
        zoneId: ZoneId,
    ): List<ScheduledMedicationDose> {
        if (regimen.status != MedicationStatus.Active || date < regimen.startDate ||
            (regimen.endDate != null && date > regimen.endDate)
        ) return emptyList()
        val times = when (val schedule = regimen.schedule) {
            is DailyMedicationSchedule -> schedule.times
            is WeeklyMedicationSchedule -> if (date.dayOfWeek in schedule.days) schedule.times else emptyList()
            else -> emptyList()
        }
        return times.sorted().map { time ->
            ScheduledMedicationDose(
                regimenId = regimen.id,
                label = regimen.label,
                dose = "${regimen.doseAmount} ${regimen.doseUnit}",
                route = regimen.route,
                localTime = time,
                scheduledAt = LocalDateTime.of(date, time).atZone(zoneId),
                source = regimen.source,
                instruction = regimen.schedule.originalInstruction,
            )
        }
    }
}
