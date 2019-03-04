package io.pleo.antaeus.core.services

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyAll
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.CustomerCreateRequest
import io.pleo.antaeus.models.CustomerUpdateRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CustomerServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchCustomer(404) } returns null
    }

    private val customerService = CustomerService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<CustomerNotFoundException> {
            customerService.fetch(404)
        }
    }

    @Test
    fun `will fetch with success`() {
        val customer = mockk<Customer>()
        every { dal.fetchCustomer(500) } returns customer

        val result = customerService.fetch(500)

        Assertions.assertSame(customer, result)

        verifyAll {
            dal.fetchCustomer(500)
        }
    }

    @Test
    fun `will fetch all with success`() {
        val customers = mockk<List<Customer>>()
        every { dal.fetchCustomers() } returns customers

        val result = customerService.fetchAll()

        Assertions.assertSame(customers, result)

        verifyAll {
            dal.fetchCustomers()
        }
    }

    @Test
    fun `will update customer`() {
        val updateRequest = mockk<CustomerUpdateRequest>()
        every { dal.updateCustomer(refEq(updateRequest)) } just Runs

        customerService.update(updateRequest)

        verifyAll {
            dal.updateCustomer(updateRequest)
        }
    }

    @Test
    fun `will create customer`() {
        val name = "name"
        val currency = Currency.DKK
        val email = "email"
        val phoneNumber = "00000000000"
        val createRequest = mockk<CustomerCreateRequest>()
        val customer = mockk<Customer>()
        every { createRequest.name } returns name
        every { createRequest.currency } returns currency
        every { createRequest.email } returns email
        every { createRequest.phoneNumber } returns phoneNumber
        every { dal.createCustomer(refEq(name), refEq(currency), refEq(email), refEq(phoneNumber)) } returns customer

        val result = customerService.create(createRequest)

        Assertions.assertSame(customer, result)

        verifyAll {
            createRequest.name
            createRequest.currency
            createRequest.email
            createRequest.phoneNumber
            dal.createCustomer(name, currency, email, phoneNumber)
        }
    }
}