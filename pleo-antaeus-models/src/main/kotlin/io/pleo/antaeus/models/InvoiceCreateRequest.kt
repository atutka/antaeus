package io.pleo.antaeus.models

data class InvoiceCreateRequest(
    val amount: Money,
    val customerId: Int
)