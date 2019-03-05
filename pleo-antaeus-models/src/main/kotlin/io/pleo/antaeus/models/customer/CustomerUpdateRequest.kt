package io.pleo.antaeus.models.customer

data class CustomerUpdateRequest(
        val id: Int,
        val name: String? = null,
        val currency: Currency? = null,
        val email: String? = null,
        val phoneNumber: String? = null
)