package com.smartphoneaichat.domain.medication

import com.smartphoneaichat.domain.model.DailyMedicationSchedule
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.MedicationStatus
import com.smartphoneaichat.domain.model.AsNeededMedicationSchedule
import com.smartphoneaichat.domain.service.MedicationScheduleMaterializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class MedicationScheduleMaterializerTest {
    private val materializer = MedicationScheduleMaterializer()

    @Test
    fun materializeToday_returnsOnlyActiveDailyOccurrencesWithinTheRegimenDates() {
        val regimen = regimen(
            schedule = DailyMedicationSchedule(listOf(LocalTime.of(8, 0), LocalTime.of(20, 0))),
            startDate = LocalDate.of(2026, 7, 20),
            endDate = LocalDate.of(2026, 7, 25),
        )

        val occurrences = materializer.materializeToday(
            regimen = regimen,
            date = LocalDate.of(2026, 7, 25),
            zoneId = ZoneId.of("Asia/Kolkata"),
        )

        assertEquals(listOf("08:00", "20:00"), occurrences.map { it.localTime.toString() })
        assertEquals(emptyList<Any>(), materializer.materializeToday(
            regimen = regimen,
            date = LocalDate.of(2026, 7, 26),
            zoneId = ZoneId.of("Asia/Kolkata"),
        ))
    }

    @Test
    fun materializeToday_handlesAsNeededSchedulesAndNormalizesDstGaps() {
        val weekly = regimen(
            schedule = DailyMedicationSchedule(listOf(LocalTime.of(2, 30))),
            startDate = LocalDate.of(2026, 3, 1),
            endDate = LocalDate.of(2026, 3, 20),
        )

        assertEquals("03:30", materializer.materializeToday(
            weekly, LocalDate.of(2026, 3, 8), ZoneId.of("America/New_York"),
        ).single().scheduledAt.toLocalTime().toString())

        val asNeeded = weekly.copy(schedule = AsNeededMedicationSchedule("Take if needed"))
        assertEquals(emptyList<Any>(), materializer.materializeToday(
            asNeeded, LocalDate.of(2026, 3, 8), ZoneId.of("America/New_York"),
        ))
    }

    private fun regimen(
        schedule: DailyMedicationSchedule,
        startDate: LocalDate,
        endDate: LocalDate,
    ) = MedicationRegimen(
        id = "med-1",
        profileId = "profile-1",
        label = "Vitamin D",
        route = "Oral",
        form = "Tablet",
        startDate = startDate,
        endDate = endDate,
        status = MedicationStatus.Active,
        source = "Manual entry",
        schedule = schedule,
    )
}
