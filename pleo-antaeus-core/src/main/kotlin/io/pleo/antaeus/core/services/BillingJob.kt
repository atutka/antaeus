package io.pleo.antaeus.core.services

import io.pleo.antaeus.models.invoice.InvoiceQuery
import io.pleo.antaeus.models.invoice.InvoiceStatus
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext

private val logger = KotlinLogging.logger {}

class BillingJob: Job {

    lateinit var billingService: BillingService
    lateinit var invoiceService: InvoiceService
    lateinit var notificationService: NotificationService

    override fun execute(context: JobExecutionContext?) {
        logger.info("Executing billing job")
        val invoiceQuery = InvoiceQuery(statuses = listOf(InvoiceStatus.PENDING))
        val invoices = invoiceService.fetch(invoiceQuery)
        val unpaidInvoiceIds = ArrayList<Int>()
        invoices.forEach {
            val chargeResult = billingService.chargeInvoice(invoice = it)
            if (!chargeResult) {
                unpaidInvoiceIds.add(it.id)
            }
        }
        if(unpaidInvoiceIds.isNotEmpty()) {
            logger.info("Some of invoices where not pay and there will be sent notification")
            notificationService.sendNotification(unpaidInvoiceIds)
        }
        logger.info("End of execution billing job")
    }

}