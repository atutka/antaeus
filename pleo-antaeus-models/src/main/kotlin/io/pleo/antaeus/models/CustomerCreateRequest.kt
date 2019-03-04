package io.pleo.antaeus.models

data class CustomerCreateRequest(
        val name: String,
        val currency: Currency,
        val email: String,
        val phoneNumber: String = ""
)