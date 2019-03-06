package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.CurrencyMismatchException
import io.pleo.antaeus.core.exceptions.CustomerNotFoundException
import io.pleo.antaeus.core.exceptions.InvoiceAlreadyPaidException
import io.pleo.antaeus.core.exceptions.InvoiceCancelledException
import io.pleo.antaeus.core.exceptions.NetworkException
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.InvoiceUpdateRequest
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
    private val paymentProvider: PaymentProvider,
    private val invoiceService: InvoiceService,
    private val currentDateTimeService: CurrentDateTimeService
) {

    fun chargeInvoice(invoice: Invoice): Boolean {
        logger.info("Starting charging for invoice id: {}", invoice.id)
        checkInvoiceStatus(invoice)
        val status = charge(invoice)
        val success = status == InvoiceStatus.PAID
        if(success) {
            invoiceService.update(InvoiceUpdateRequest(id = invoice.id, status = status,
                    successfulChargeDate = currentDateTimeService.getCurrentLocalDateTime()))
            logger.info("Invoice with id {} was successfully charged", invoice.id)
        }
        else {
            invoiceService.update(InvoiceUpdateRequest(id = invoice.id, status = status))
        }
        logger.info("Ending charging for invoice")
        return success
    }

    private fun checkInvoiceStatus(invoice: Invoice) {
        if (invoice.status == InvoiceStatus.PAID) {
            logger.error("Invoice was paid and cannot be charged again")
            throw InvoiceAlreadyPaidException(invoiceId = invoice.id)
        }
        if (invoice.status == InvoiceStatus.CANCELED) {
            logger.error("Invoice was canceled and cannot be charged")
            throw InvoiceCancelledException(invoiceId = invoice.id)
        }
    }

    private fun charge(invoice: Invoice): InvoiceStatus {
        try {
            if (!paymentProvider.charge(invoice)) {
                logger.error("Customer with id: {} didn't have enough money on account", invoice.customerId)
                return InvoiceStatus.UNPAID_LOW_ACCOUNT_BALANCE
            }
            return InvoiceStatus.PAID
        } catch (e: CustomerNotFoundException) {
            logger.error("Customer with id: {} not exists", invoice.customerId)
            return InvoiceStatus.UNPAID_CUSTOMER_NOT_EXISTS
        } catch (e: CurrencyMismatchException) {
            logger.error("Customer with id: {} couldn't be charge because of different currency on his account", invoice.customerId)
            return InvoiceStatus.UNPAID_MISMATCH_CURRENCY
        } catch (e: NetworkException) {
            logger.error("Customer with id: {} couldn't be charge because of network problem", invoice.customerId)
            return InvoiceStatus.UNPAID_NETWORK_ERROR
        } catch (e: Exception) {
            logger.error("Unknown error occurred", e)
            return InvoiceStatus.UNPAID_ERROR
        }
    }

}