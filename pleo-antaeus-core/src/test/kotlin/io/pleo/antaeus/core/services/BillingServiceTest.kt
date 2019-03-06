package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verifyAll
import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceAlreadyPaidException
import io.pleo.antaeus.core.exceptions.InvoiceCancelledException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.InvoiceUpdateRequest
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BillingServiceTest {
    private val paymentProvider = mockk<PaymentProvider>()
    private val invoiceService = mockk<InvoiceService>()
    private val currentDateTimeService = mockk<CurrentDateTimeService>()

    private val billingService = BillingService(paymentProvider = paymentProvider,
            invoiceService = invoiceService,
            currentDateTimeService = currentDateTimeService)

    @Test
    fun `will charge with success`() {
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        val currentLocalDateTime = LocalDateTime(2019, 3, 5, 18, 0,0)
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { paymentProvider.charge(refEq(invoice)) } returns true
        every { currentDateTimeService.getCurrentLocalDateTime() } returns currentLocalDateTime
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.PAID, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(currentLocalDateTime, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            paymentProvider.charge(invoice)
            currentDateTimeService.getCurrentLocalDateTime()
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will charge but fail due to account low balance`() {
        val invoiceId = 100
        val customerId = 200
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { invoice.customerId } returns customerId
        every { paymentProvider.charge(refEq(invoice)) } returns false
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.UNPAID_LOW_ACCOUNT_BALANCE, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(null, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            invoice.customerId
            paymentProvider.charge(invoice)
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will charge and fail due to not existing customer`() {
        val invoiceId = 100
        val customerId = 200
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        val customerNotFoundException = CustomerNotFoundException(customerId)
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { invoice.customerId } returns customerId
        every { paymentProvider.charge(refEq(invoice)) } throws customerNotFoundException
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.UNPAID_CUSTOMER_NOT_EXISTS, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(null, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            invoice.customerId
            paymentProvider.charge(invoice)
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will charge and fails due to currency mismatch`() {
        val invoiceId = 100
        val customerId = 200
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        val currencyMismatchException = CurrencyMismatchException(invoiceId, customerId)
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { invoice.customerId } returns customerId
        every { paymentProvider.charge(refEq(invoice)) } throws currencyMismatchException
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.UNPAID_MISMATCH_CURRENCY, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(null, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            invoice.customerId
            paymentProvider.charge(invoice)
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will charge and fail due to network problems`() {
        val invoiceId = 100
        val customerId = 200
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        val networkException = NetworkException()
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { invoice.customerId } returns customerId
        every { paymentProvider.charge(refEq(invoice)) } throws networkException
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.UNPAID_NETWORK_ERROR, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(null, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            invoice.customerId
            paymentProvider.charge(invoice)
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will charge and fails due to unknown error`() {
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        val invoiceUpdateRequestSlot = slot<InvoiceUpdateRequest>()
        val exception = Exception()
        every { invoice.status } returns InvoiceStatus.PENDING
        every { invoice.id } returns invoiceId
        every { paymentProvider.charge(refEq(invoice)) } throws exception
        every { invoiceService.update(capture(invoiceUpdateRequestSlot)) } just runs

        billingService.chargeInvoice(invoice)

        val invoiceUpdateRequest = invoiceUpdateRequestSlot.captured
        assertEquals(InvoiceStatus.UNPAID_ERROR, invoiceUpdateRequest.status)
        assertEquals(invoiceId, invoiceUpdateRequest.id)
        assertEquals(null, invoiceUpdateRequest.successfulChargeDate)

        verifyAll {
            invoice.id
            invoice.status
            paymentProvider.charge(invoice)
            invoiceService.update(invoiceUpdateRequest)
        }
    }

    @Test
    fun `will throw if invoice is already paid`() {
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        every { invoice.status } returns InvoiceStatus.PAID
        every { invoice.id } returns invoiceId

        assertThrows<InvoiceAlreadyPaidException> { billingService.chargeInvoice(invoice) }

        verifyAll {
            invoice.id
            invoice.status
        }
    }

    @Test
    fun `will throw if invoice is canceled`() {
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        every { invoice.id } returns invoiceId
        every { invoice.status } returns InvoiceStatus.CANCELED

        assertThrows<InvoiceCancelledException> { billingService.chargeInvoice(invoice) }

        verifyAll {
            invoice.id
            invoice.status
        }
    }
}