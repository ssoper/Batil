package com.seansoper.batil

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

object OptionsCalendar {

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
