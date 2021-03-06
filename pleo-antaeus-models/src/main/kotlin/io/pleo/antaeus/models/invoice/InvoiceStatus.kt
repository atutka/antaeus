package io.pleo.antaeus.models.invoice

enum class InvoiceStatus {
    PENDING,
    PAID,
    UNPAID_CUSTOMER_NOT_EXISTS,
    UNPAID_LOW_ACCOUNT_BALANCE,
    UNPAID_MISMATCH_CURRENCY,
    UNPAID_NETWORK_ERROR,
    UNPAID_ERROR,
    CANCELED
}
