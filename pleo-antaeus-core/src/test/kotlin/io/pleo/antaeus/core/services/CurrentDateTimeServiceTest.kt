package io.pleo.antaeus.core.services

import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrentDateTimeServiceTest {

    private val currentDateTimeService = CurrentDateTimeService()

    @Test
    fun `will get current date time`() {
        val expectedLocalDateTime = LocalDateTime.now()
        val result = currentDateTimeService.getCurrentLocalDateTime()

        assertEquals(expectedLocalDateTime.year, result.year)
        assertEquals(expectedLocalDateTime.monthOfYear, result.monthOfYear)
        assertEquals(expectedLocalDateTime.dayOfMonth, result.dayOfMonth)
        assertEquals(expectedLocalDateTime.hourOfDay, result.hourOfDay)
        assertEquals(expectedLocalDateTime.minuteOfHour, result.minuteOfHour)
        assertEquals(expectedLocalDateTime.secondOfMinute, result.secondOfMinute)
    }
}