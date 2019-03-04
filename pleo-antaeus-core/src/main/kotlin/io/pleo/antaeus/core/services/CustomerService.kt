/*
    Implements endpoints related to customers.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.CustomerCreateRequest
import io.pleo.antaeus.models.CustomerUpdateRequest

class CustomerService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Customer> {
       return dal.fetchCustomers()
    }

    fun fetch(id: Int): Customer {
        return dal.fetchCustomer(id) ?: throw CustomerNotFoundException(id)
    }

    fun create(createRequest: CustomerCreateRequest): Customer {
        return dal.createCustomer(
                name = createRequest.name,
                currency = createRequest.currency,
                email = createRequest.email,
                phoneNumber = createRequest.phoneNumber
        )!!
    }

    fun update(updateRequest: CustomerUpdateRequest) {
        dal.updateCustomer(updateRequest)
    }
}
