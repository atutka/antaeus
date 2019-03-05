package io.pleo.antaeus.models.invoice

data class InvoiceQuery(
    val statuses: List<InvoiceStatus> = listOf()
)