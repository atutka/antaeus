/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.core.services.BillingJob
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CurrentDateTimeService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.EmailService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.core.services.NotificationService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.quartz.CronScheduleBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.quartz.impl.StdSchedulerFactory
import org.quartz.simpl.SimpleJobFactory
import org.quartz.spi.TriggerFiredBundle
import setupInitialData
import java.sql.Connection
import java.util.*


private const val JOB_NAME = "billingJob"
private const val JOB_GROUP_NAME = "billingGroup"
private const val JOB_TRIGGER_NAME = "triggerBillingJob"

fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
        .connect("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")
        .also {
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(it) {
                addLogger(StdOutSqlLogger)
                // Drop all existing tables to ensure a clean slate on each run
                SchemaUtils.drop(*tables)
                // Create all tables
                SchemaUtils.create(*tables)
            }
        }

    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    val properties = loadProperties()

    // Get third parties
    val paymentProvider = getPaymentProvider()

    // Create core services
    val invoiceService = InvoiceService(dal = dal)
    val customerService = CustomerService(dal = dal)
    val currentDateTimeService = CurrentDateTimeService()
    val emailService = EmailService(currentDateTimeService = currentDateTimeService, properties = properties)
    val notificationService = NotificationService(properties = properties, emailService = emailService)

    // This is _your_ billing service to be included where you see fit
    val billingService = BillingService(paymentProvider = paymentProvider,
            invoiceService = invoiceService,
            currentDateTimeService = currentDateTimeService)
    scheduleBillingJob(properties = properties,
            billingService = billingService,
            invoiceService = invoiceService,
            notificationService = notificationService)

    // Create REST web service
    AntaeusRest(
        invoiceService = invoiceService,
        customerService = customerService,
        billingService = billingService,
        properties = properties
    ).run()
}

private fun scheduleBillingJob(properties: Properties,
                               billingService: BillingService,
                               invoiceService: InvoiceService,
                               notificationService: NotificationService) {
    val billingJobFactory = BillingJobFactory(billingService, invoiceService, notificationService)
    val scheduler = StdSchedulerFactory().scheduler
    scheduler.setJobFactory(billingJobFactory)
    val billingJob = JobBuilder.newJob(BillingJob::class.java)
            .withIdentity(JOB_NAME, JOB_GROUP_NAME)
            .build()
    val trigger = TriggerBuilder.newTrigger()
            .withIdentity(JOB_TRIGGER_NAME, JOB_GROUP_NAME)
            .withSchedule(CronScheduleBuilder.cronSchedule(properties.getProperty("billingjob.cron")))
            .forJob(JOB_NAME, JOB_GROUP_NAME)
            .build()
    scheduler.scheduleJob(billingJob, trigger)
    scheduler.start()
}

private fun loadProperties(): Properties {
    val propertiesFile = Thread.currentThread().contextClassLoader.getResourceAsStream("application.properties")
    val properties = Properties()
    properties.load(propertiesFile)
    return properties
}

private class BillingJobFactory(
        private val billingService: BillingService,
        private val invoiceService: InvoiceService,
        private val notificationService: NotificationService
) : SimpleJobFactory() {

    override fun newJob(bundle: TriggerFiredBundle?, Scheduler: Scheduler?): Job {
        val billingJob = super.newJob(bundle, Scheduler) as BillingJob
        billingJob.billingService = billingService
        billingJob.invoiceService = invoiceService
        billingJob.notificationService = notificationService
        return billingJob
    }
}

