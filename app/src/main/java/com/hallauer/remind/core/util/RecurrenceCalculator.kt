package com.hallauer.remind.core.util

import com.hallauer.remind.data.local.entity.ContactScheduleEntity
import com.hallauer.remind.data.local.entity.GroupBucket
import com.hallauer.remind.data.local.entity.RecurrenceType
import com.hallauer.remind.data.local.entity.TimeMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.random.Random

data class CalculatedOccurrence(
    val targetDateTime: LocalDateTime,
    val epochMillis: Long,
    val dateIso: String,
    val lockedMinute: Int
)

data class VirtualCalendarOccurrence(
    val contactScheduleId: Long,
    val displayName: String,
    val bucket: GroupBucket,
    val projectedDate: LocalDate,
    val timeSummary: String,
    val placeholderNote: String = "Phrase will roll after previous message executes"
)

object RecurrenceCalculator {

    /**
     * Calculates the immediate next target occurrence for a ContactScheduleEntity.
     * Enforces the 7:00 AM Recovery Same-Day Skip rule:
     * If the contact already had a dispatch today (e.g. at 7:00 AM following a blackout recovery),
     * the search for the next recurrence date starts strictly from tomorrow (LocalDate.now().plusDays(1)).
     */
    fun computeNextOccurrence(
        schedule: ContactScheduleEntity,
        fromDateTime: LocalDateTime = LocalDateTime.now(),
        randomGenerator: Random = Random.Default
    ): CalculatedOccurrence {
        val today = fromDateTime.toLocalDate()
        val lastDispatchedDate = schedule.lastDispatchedDateIso?.let {
            runCatching { LocalDate.parse(it) }.getOrNull()
        }

        // Clamp target hour to safe waking hours 7..22 (7:00 AM to 10:00 PM)
        val clampedHour = schedule.targetHour.coerceIn(7, 22)

        // Lock in minute: exact target minute or random 0..59 within 1-hr window
        val lockedMinute = when (schedule.timeMode) {
            TimeMode.EXACT_TIME -> schedule.targetMinute.coerceIn(0, 59)
            TimeMode.RANDOM_WINDOW_1HR -> randomGenerator.nextInt(0, 60)
        }

        // If contact already dispatched today, start searching strictly tomorrow to prevent double texting
        val searchStartDate: LocalDate = if (lastDispatchedDate != null && lastDispatchedDate == today) {
            today.plusDays(1)
        } else {
            today
        }

        val nextDate = findNextRecurrenceDate(
            schedule = schedule,
            startSearchFrom = searchStartDate,
            targetHour = clampedHour,
            targetMinute = lockedMinute,
            now = fromDateTime
        )

        val targetDateTime = LocalDateTime.of(nextDate, LocalTime.of(clampedHour, lockedMinute))
        val zoneId = ZoneId.systemDefault()
        val epochMillis = targetDateTime.atZone(zoneId).toInstant().toEpochMilli()

        return CalculatedOccurrence(
            targetDateTime = targetDateTime,
            epochMillis = epochMillis,
            dateIso = nextDate.toString(),
            lockedMinute = lockedMinute
        )
    }

    private fun findNextRecurrenceDate(
        schedule: ContactScheduleEntity,
        startSearchFrom: LocalDate,
        targetHour: Int,
        targetMinute: Int,
        now: LocalDateTime
    ): LocalDate {
        var candidateDate = startSearchFrom

        when (schedule.recurrenceType) {
            RecurrenceType.DAILY -> {
                val candidateTime = LocalDateTime.of(candidateDate, LocalTime.of(targetHour, targetMinute))
                return if (candidateTime.isAfter(now)) candidateDate else candidateDate.plusDays(1)
            }

            RecurrenceType.EVERY_N_DAYS -> {
                val interval = maxOf(1, schedule.intervalN)
                val candidateTime = LocalDateTime.of(candidateDate, LocalTime.of(targetHour, targetMinute))
                return if (candidateTime.isAfter(now)) candidateDate else candidateDate.plusDays(interval.toLong())
            }

            RecurrenceType.WEEKLY -> {
                val allowedDays = parseDaysOfWeek(schedule.daysOfWeekCsv)
                if (allowedDays.isEmpty()) return candidateDate.plusDays(1)

                var testDate = candidateDate
                for (i in 0..14) {
                    if (allowedDays.contains(testDate.dayOfWeek)) {
                        val testTime = LocalDateTime.of(testDate, LocalTime.of(targetHour, targetMinute))
                        if (testTime.isAfter(now)) {
                            return testDate
                        }
                    }
                    testDate = testDate.plusDays(1)
                }
                return candidateDate.plusDays(7)
            }

            RecurrenceType.EVERY_N_WEEKS -> {
                val allowedDays = parseDaysOfWeek(schedule.daysOfWeekCsv)
                val targetDayOfWeek = allowedDays.firstOrNull() ?: DayOfWeek.MONDAY
                val intervalWeeks = maxOf(1, schedule.intervalN)

                var testDate = candidateDate.with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
                val testTime = LocalDateTime.of(testDate, LocalTime.of(targetHour, targetMinute))
                return if (testTime.isAfter(now)) {
                    testDate
                } else {
                    testDate.plusWeeks(intervalWeeks.toLong())
                }
            }

            RecurrenceType.MONTHLY_DAY -> {
                return calculateMonthlyDay(candidateDate, schedule.dayOfMonth, targetHour, targetMinute, now)
            }

            RecurrenceType.YEARLY_DATE -> {
                val targetMonth = schedule.monthOfYear.coerceIn(1, 12)
                val targetDay = schedule.dayOfMonth.coerceIn(1, 31)

                var targetYear = candidateDate.year
                var testDate = resolveValidDate(targetYear, targetMonth, targetDay)
                val testTime = LocalDateTime.of(testDate, LocalTime.of(targetHour, targetMinute))

                return if (testTime.isAfter(now)) {
                    testDate
                } else {
                    resolveValidDate(targetYear + 1, targetMonth, targetDay)
                }
            }
        }
    }

    private fun calculateMonthlyDay(
        candidateDate: LocalDate,
        dayOfMonth: Int,
        targetHour: Int,
        targetMinute: Int,
        now: LocalDateTime
    ): LocalDate {
        var currentMonthDate = candidateDate
        for (monthOffset in 0..12) {
            val testMonth = currentMonthDate.plusMonths(monthOffset.toLong())
            val resolvedDay = if (dayOfMonth == -1) {
                testMonth.with(TemporalAdjusters.lastDayOfMonth()).dayOfMonth
            } else {
                dayOfMonth.coerceIn(1, testMonth.lengthOfMonth())
            }
            val testDate = testMonth.withDayOfMonth(resolvedDay)
            val testTime = LocalDateTime.of(testDate, LocalTime.of(targetHour, targetMinute))
            if (testTime.isAfter(now)) {
                return testDate
            }
        }
        return candidateDate.plusMonths(1)
    }

    private fun resolveValidDate(year: Int, month: Int, day: Int): LocalDate {
        val baseMonth = LocalDate.of(year, month, 1)
        val validDay = day.coerceIn(1, baseMonth.lengthOfMonth())
        return LocalDate.of(year, month, validDay)
    }

    private fun parseDaysOfWeek(csv: String): Set<DayOfWeek> {
        if (csv.isBlank()) return setOf(DayOfWeek.MONDAY)
        return csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .map { DayOfWeek.of(it) }
            .toSet()
    }

    /**
     * Projects future occurrences for a contact beyond their single locked-in PENDING occurrence
     * through the visible calendar month.
     */
    fun projectFutureOccurrences(
        schedule: ContactScheduleEntity,
        lockedPendingDate: LocalDate,
        monthStart: LocalDate,
        monthEnd: LocalDate
    ): List<VirtualCalendarOccurrence> {
        if (!schedule.isActive) return emptyList()

        val results = mutableListOf<VirtualCalendarOccurrence>()
        var cursorDate = lockedPendingDate.plusDays(1)

        val timeSummary = when (schedule.timeMode) {
            TimeMode.EXACT_TIME -> {
                val hour12 = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
                val amPm = if (schedule.targetHour >= 12) "PM" else "AM"
                String.format("%d:%02d %s", hour12, schedule.targetMinute, amPm)
            }
            TimeMode.RANDOM_WINDOW_1HR -> {
                val startHour12 = if (schedule.targetHour % 12 == 0) 12 else schedule.targetHour % 12
                val endHour = schedule.targetHour + 1
                val endHour12 = if (endHour % 12 == 0) 12 else endHour % 12
                val amPmStart = if (schedule.targetHour >= 12) "PM" else "AM"
                val amPmEnd = if (endHour >= 12) "PM" else "AM"
                String.format("%d %s - %d %s Window", startHour12, amPmStart, endHour12, amPmEnd)
            }
        }

        // Limit loop safety bounds to prevent runaway while projecting
        var iterations = 0
        while (cursorDate <= monthEnd && iterations < 365) {
            iterations++
            val nextProjected = findNextRecurrenceDate(
                schedule = schedule,
                startSearchFrom = cursorDate,
                targetHour = schedule.targetHour,
                targetMinute = 0,
                now = cursorDate.atStartOfDay().minusSeconds(1)
            )

            if (nextProjected > monthEnd) break

            if (nextProjected >= monthStart && nextProjected > lockedPendingDate) {
                results.add(
                    VirtualCalendarOccurrence(
                        contactScheduleId = schedule.id,
                        displayName = schedule.displayName,
                        bucket = schedule.bucket,
                        projectedDate = nextProjected,
                        timeSummary = timeSummary
                    )
                )
            }
            cursorDate = nextProjected.plusDays(1)
        }

        return results
    }
}
