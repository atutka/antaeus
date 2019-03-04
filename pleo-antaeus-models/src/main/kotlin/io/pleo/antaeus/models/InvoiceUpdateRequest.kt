package io.pleo.antaeus.models

import org.joda.time.LocalDateTime

data class InvoiceUpdateRequest(
    val id: Int,
    val status: InvoiceStatus? = null,
    val successfulChargeDate: LocalDateTime? = null
)