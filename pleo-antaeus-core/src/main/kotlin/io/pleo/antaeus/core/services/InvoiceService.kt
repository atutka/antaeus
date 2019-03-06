/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.core.exceptions.PaidInvoiceCannotBeCancelledException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceCreateRequest
import io.pleo.antaeus.models.invoice.InvoiceQuery
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.InvoiceUpdateRequest

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun update(updateRequest: InvoiceUpdateRequest) {
        val invoice = fetch(updateRequest.id)
        if(invoice.status == InvoiceStatus.PAID && updateRequest.status == InvoiceStatus.CANCELED) {
            throw PaidInvoiceCannotBeCancelledException(invoiceId = updateRequest.id)
        }
        dal.updateInvoice(updateRequest)
    }

    fun fetch(invoiceQuery: InvoiceQuery): List<Invoice> {
        return dal.fetchInvoices(invoiceQuery = invoiceQuery)
    }

    fun create(createRequest: InvoiceCreateRequest): Invoice {
        return dal.createInvoice(createRequest)
    }
}
