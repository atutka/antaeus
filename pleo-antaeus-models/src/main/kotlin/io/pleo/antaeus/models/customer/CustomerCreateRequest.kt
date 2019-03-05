package io.pleo.antaeus.models.customer

data class CustomerCreateRequest(
        val name: String,
        val currency: Currency,
        val email: String,
        val phoneNumber: String = ""
)