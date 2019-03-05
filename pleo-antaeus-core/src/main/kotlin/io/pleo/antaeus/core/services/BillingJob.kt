package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.invoice.InvoiceQuery
import io.pleo.antaeus.models.invoice.InvoiceStatus
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class BillingJob(
        private val billingService: BillingService,
        private val invoiceService: InvoiceService
): Job {

    override fun execute(context: JobExecutionContext?) {
        logger.info("Executing billing job")
        val invoiceQuery = InvoiceQuery(statuses = listOf(InvoiceStatus.PENDING))
        val invoices = invoiceService.fetch(invoiceQuery)
        invoices.forEach(billingService::chargeInvoice)
        logger.info("End of execution billing job")
    }


}