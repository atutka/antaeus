package io.pleo.antaeus.models.invoice

import io.pleo.antaeus.models.customer.Currency
import java.math.BigDecimal

data class Money(
    val value: BigDecimal,
    val currency: Currency
)
