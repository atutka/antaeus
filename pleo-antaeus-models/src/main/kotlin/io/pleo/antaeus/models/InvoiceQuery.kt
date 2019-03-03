package io.pleo.antaeus.models

data class InvoiceQuery(
    val statuses: List<InvoiceStatus> = listOf()
)