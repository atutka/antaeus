package io.pleo.antaeus.models.customer

data class Customer(
        val id: Int,
        val name: String,
        val currency: Currency,
        val email: String,
        val phoneNumber: String? = ""
)
