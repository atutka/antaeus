package io.pleo.antaeus.models.invoice

data class Invoice(
        val id: Int,
        val customerId: Int,
        val amount: Money,
        val status: InvoiceStatus,
        val successfulChargeDate: String?
)
