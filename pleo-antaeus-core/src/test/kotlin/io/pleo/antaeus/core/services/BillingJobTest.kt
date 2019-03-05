package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verifyAll
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceQuery
import io.pleo.antaeus.models.invoice.InvoiceStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class BillingJobTest {
    private val billingService = mockk<BillingService>()
    private val invoiceService = mockk<InvoiceService>()

    private val billingJob = BillingJob(billingService = billingService, invoiceService = invoiceService)

    @Test
    fun `will execute job`() {
        val invoiceQuerySlot = slot<InvoiceQuery>()
        val invoice = mockk<Invoice>()
        val invoices = listOf(invoice)
        every { invoiceService.fetch(capture(invoiceQuerySlot)) } returns invoices
        every { billingService.chargeInvoice(refEq(invoice)) } just runs


        billingJob.execute(null)

        val invoiceQuery = invoiceQuerySlot.captured
        Assertions.assertEquals(1, invoiceQuery.statuses.size)
        assertSame(InvoiceStatus.PENDING, invoiceQuery.statuses.first())

        verifyAll {
            invoiceService.fetch(invoiceQuery)
            billingService.chargeInvoice(invoice)
        }
    }
}