package testHelper

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

data class TestOption(
    val symbol: String,
    val year: Int,
    val month: Int,
    val day: Int
) {
    val expiry: ZonedDateTime
        get() {
            return ZonedDateTime.of(
                LocalDate.of(year, month, day),
                LocalTime.of(16, 0),
                ZoneId.of("America/New_York")
            )
        }
}
