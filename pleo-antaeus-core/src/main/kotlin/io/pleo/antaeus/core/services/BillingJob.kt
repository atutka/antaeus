package io.pleo.antaeus.core.services

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

        
        logger.info("End of execution billing job")
    }


}