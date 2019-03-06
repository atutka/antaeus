package io.pleo.antaeus.core.services

import io.mockk.clearAllMocks
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BillingJobTest {

    private val billingService = mockk<BillingService>()
    private val invoiceService = mockk<InvoiceService>()
    private val notificationService = mockk<NotificationService>()

    private val billingJob = BillingJob()

    @BeforeEach
    internal fun setUp()
    {
        billingJob.billingService = billingService
        billingJob.invoiceService = invoiceService
        billingJob.notificationService = notificationService
        clearAllMocks()
    }


    @Test
    fun `will execute job`() {
        val invoiceQuerySlot = slot<InvoiceQuery>()
        val invoice = mockk<Invoice>()
        val invoices = listOf(invoice)
        every { invoiceService.fetch(capture(invoiceQuerySlot)) } returns invoices
        every { billingService.chargeInvoice(refEq(invoice)) } returns true


        billingJob.execute(null)

        val invoiceQuery = invoiceQuerySlot.captured
        Assertions.assertEquals(1, invoiceQuery.statuses.size)
        assertSame(InvoiceStatus.PENDING, invoiceQuery.statuses.first())

        verifyAll {
            invoiceService.fetch(invoiceQuery)
            billingService.chargeInvoice(invoice)
        }
    }

    @Test
    fun `will execute job with sending notification`() {
        val invoiceId = 100
        val invoiceQuerySlot = slot<InvoiceQuery>()
        val invoice = mockk<Invoice>()
        val invoices = listOf(invoice)
        val invoiceIdsSlot = slot<List<Int>>()
        every { invoiceService.fetch(capture(invoiceQuerySlot)) } returns invoices
        every { billingService.chargeInvoice(refEq(invoice)) } returns false
        every { invoice.id } returns invoiceId
        every { notificationService.sendNotification(capture(invoiceIdsSlot)) } just runs


        billingJob.execute(null)

        val invoiceQuery = invoiceQuerySlot.captured
        Assertions.assertEquals(1, invoiceQuery.statuses.size)
        assertSame(InvoiceStatus.PENDING, invoiceQuery.statuses.first())

        val invoiceIds = invoiceIdsSlot.captured
        assertEquals(1, invoiceIds.size)
        assertEquals(invoiceId, invoiceIds.first())

        verifyAll {
            invoice.id
            invoiceService.fetch(invoiceQuery)
            billingService.chargeInvoice(invoice)
            notificationService.sendNotification(invoiceIds)
        }
    }
}