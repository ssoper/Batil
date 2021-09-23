package com.seansoper.batil

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

// TODO: Add method to calculate next weekly
// TODO: Add method to calculate monthly for any given month
object OptionsCalendar {

    /**
     * Return the next third Friday in a month, a date associated with expiring monthly contracts.
     */
    fun nextMonthly(now: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"))): ZonedDateTime {
        val result = now
            .with(TemporalAdjusters.firstDayOfMonth())
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .plusDays(14)

        return if (result.month == now.month && result.dayOfMonth < now.dayOfMonth) {
            now
                .with(TemporalAdjusters.firstDayOfNextMonth())
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .plusDays(14)
        } else {
            result
        }
    }
}
