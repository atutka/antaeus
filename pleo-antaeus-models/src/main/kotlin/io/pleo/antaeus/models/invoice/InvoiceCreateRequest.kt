package io.pleo.antaeus.models.invoice

data class InvoiceCreateRequest(
        val amount: Money,
        val customerId: Int
)