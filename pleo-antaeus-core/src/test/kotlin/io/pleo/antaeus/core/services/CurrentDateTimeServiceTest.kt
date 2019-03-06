package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockkStatic
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CurrentDateTimeServiceTest {

    private val currentDateTimeService = CurrentDateTimeService()

    @Test
    fun `will get current date time`() {
        mockkStatic(LocalDateTime::class)
        val localDateTime = LocalDateTime(2019, 6, 3, 17, 0 ,0)

        every { LocalDateTime.now() } returns localDateTime
        
        val result = currentDateTimeService.getCurrentLocalDateTime()

        assertEquals(localDateTime, result)
    }
}