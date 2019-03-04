/*
    Configures the rest app along with basic exception handling and URL endpoints.
 */

package io.pleo.antaeus.rest

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.apibuilder.ApiBuilder.put
import io.pleo.antaeus.core.exceptions.EntityNotFoundException
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.models.CustomerCreateRequest
import io.pleo.antaeus.models.CustomerUpdateRequest
import io.pleo.antaeus.models.InvoiceCreateRequest
import io.pleo.antaeus.models.InvoiceQuery
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceUpdateRequest
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class AntaeusRest (
    private val invoiceService: InvoiceService,
    private val customerService: CustomerService,
    private val billingService: BillingService
) : Runnable {

    override fun run() {
        app.start(7000)
    }

    // Set up Javalin rest app
    private val app = Javalin
        .create()
        .apply {
            // InvoiceNotFoundException: return 404 HTTP status code
            exception(EntityNotFoundException::class.java) { _, ctx ->
                ctx.status(404)
            }
            // Unexpected exception: return HTTP 500
            exception(Exception::class.java) { e, _ ->
                logger.error(e) { "Internal server error" }
            }
            // On 404: return message
            error(404) { ctx -> ctx.json("not found") }
        }

    init {
        // Set up URL endpoints for the rest app
        app.routes {
           path("rest") {
               // Route to check whether the app is running
               // URL: /rest/health
               get("health") {
                   it.json("ok")
               }

               // V1
               path("v1") {
                   path("invoices") {
                       // URL: /rest/v1/invoices
                       get {
                           it.json(invoiceService.fetchAll())
                       }

                       // URL: /rest/v1/invoices/id/{:id}
                       get("id/:id") {
                          it.json(invoiceService.fetch(it.pathParam("id").toInt()))
                       }

                       // URL: /rest/v1/invoices/unpaid
                       get("unpaid") {
                           it.json(invoiceService.fetch(InvoiceQuery(statuses = listOf(InvoiceStatus.UNPAID_NETWORK_ERROR,
                                   InvoiceStatus.UNPAID_LOW_ACCOUNT_BALANCE, InvoiceStatus.UNPAID_MISMATCH_CURRENCY,
                                   InvoiceStatus.UNPAID_CUSTOMER_NOT_EXISTS, InvoiceStatus.UNPAID_ERROR))))
                       }

                       // URL: /rest/v1/invoices/charge/id/{:id}
                       post("charge/id/:id") {
                           it.json(billingService.chargeInvoice(invoiceService.fetch(it.pathParam("id").toInt())))
                       }

                       // URL: /rest/v1/invoices/cancel/id/{:id}
                       post("cancel/id/:id") {
                           it.json(invoiceService.update(InvoiceUpdateRequest(id = it.pathParam("id").toInt(), status = InvoiceStatus.CANCELED)))
                       }

                       // URL: /rest/v1/invoices
                       put {
                          it.json(invoiceService.create(it.bodyAsClass(InvoiceCreateRequest::class.java)))
                       }

                   }

                   path("customers") {
                       // URL: /rest/v1/customers
                       get {
                           it.json(customerService.fetchAll())
                       }

                       // URL: /rest/v1/customers/id/{:id}
                       get("id/:id") {
                           it.json(customerService.fetch(it.pathParam("id").toInt()))
                       }

                       // URL: /rest/v1/customers
                       put {
                           it.json(customerService.create(it.bodyAsClass(CustomerCreateRequest::class.java)))
                       }

                       // URL: /rest/v1/customers
                       post {
                           it.json(customerService.update(it.bodyAsClass(CustomerUpdateRequest::class.java)))
                       }
                   }
               }
           }
        }
    }
}
