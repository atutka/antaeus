package io.pleo.antaeus.core.services

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifyAll
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.PaidInvoiceCannotBeCancelledException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceCreateRequest
import io.pleo.antaeus.models.invoice.InvoiceQuery
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.InvoiceUpdateRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal> {
        every { fetchInvoice(404) } returns null
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if invoice is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `will fetch with success`() {
        val invoice = mockk<Invoice>()
        every { dal.fetchInvoice(500) } returns invoice

        val result = invoiceService.fetch(500)

        Assertions.assertSame(invoice, result)

        verifyAll {
            dal.fetchInvoice(500)
        }
    }

    @Test
    fun `will fetch all with success`() {
        val invoices = mockk<List<Invoice>>()
        every { dal.fetchInvoices() } returns invoices

        val result = invoiceService.fetchAll()

        Assertions.assertSame(invoices, result)

        verifyAll {
            dal.fetchInvoices()
        }
    }

    @Test
    fun `will fetch with query with success`() {
        val invoices = mockk<List<Invoice>>()
        val invoiceQuery = mockk<InvoiceQuery>()
        every { dal.fetchInvoices(refEq(invoiceQuery)) } returns invoices

        val result = invoiceService.fetch(invoiceQuery)

        Assertions.assertSame(invoices, result)

        verifyAll {
            dal.fetchInvoices(invoiceQuery)
        }
    }

    @Test
    fun `will update invoice`() {
        val updateRequest = mockk<InvoiceUpdateRequest>()
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        every { dal.updateInvoice(refEq(updateRequest)) } just Runs
        every { updateRequest.id } returns invoiceId
        every { dal.fetchInvoice(eq(invoiceId)) } returns invoice
        every { invoice.status } returns InvoiceStatus.PENDING

        invoiceService.update(updateRequest)

        verifyAll {
            dal.updateInvoice(updateRequest)
            dal.fetchInvoice(invoiceId)
            updateRequest.id
            invoice.status
        }
    }

    @Test
    fun `will throw paid invoice cannot be cancelled exception`() {
        val updateRequest = mockk<InvoiceUpdateRequest>()
        val invoiceId = 100
        val invoice = mockk<Invoice>()
        every { updateRequest.id } returns invoiceId
        every { updateRequest.status } returns InvoiceStatus.CANCELED
        every { dal.fetchInvoice(eq(invoiceId)) } returns invoice
        every { invoice.status } returns InvoiceStatus.PAID

        assertThrows<PaidInvoiceCannotBeCancelledException> {
            invoiceService.update(updateRequest)
        }

        verifyAll {
            dal.fetchInvoice(invoiceId)
            updateRequest.id
            updateRequest.status
            invoice.status
        }
    }

    @Test
    fun `will create invoice`() {
        val createRequest = mockk<InvoiceCreateRequest>()
        val invoice = mockk<Invoice>()
        every { dal.createInvoice(refEq(createRequest)) } returns invoice

        val result = invoiceService.create(createRequest)

        Assertions.assertSame(invoice, result)

        verifyAll {
            dal.createInvoice(createRequest)
        }
    }
}