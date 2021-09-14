import com.seansoper.batil.OptionsCalendar
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters

class OptionsCalendarTest : StringSpec({

    val now = ZonedDateTime.now(ZoneId.of("America/New_York"))

    "monthly for this month" {
        val curr = now.with(TemporalAdjusters.firstDayOfMonth())
        val expected = now
            .with(TemporalAdjusters.firstDayOfMonth())
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .plusDays(14)

        val result = OptionsCalendar.nextMonthly(curr)
        result.month.shouldBe(expected.month)
        result.dayOfMonth.shouldBe(expected.dayOfMonth)
    }

    "monthly for next month" {
        val curr = now.with(TemporalAdjusters.lastDayOfMonth())
        val expected = now
            .with(TemporalAdjusters.firstDayOfNextMonth())
            .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .plusDays(14)

        val result = OptionsCalendar.nextMonthly(curr)
        result.month.shouldBe(expected.month)
        result.dayOfMonth.shouldBe(expected.dayOfMonth)
    }
})
