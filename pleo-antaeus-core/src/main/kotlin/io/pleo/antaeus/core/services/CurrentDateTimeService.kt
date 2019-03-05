package io.pleo.antaeus.core.services

import org.joda.time.LocalDateTime


class CurrentDateTimeService {

    fun getCurrentLocalDateTime(): LocalDateTime {
        return LocalDateTime.now()
    }
}