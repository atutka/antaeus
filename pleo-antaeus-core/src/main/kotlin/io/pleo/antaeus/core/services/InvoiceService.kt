/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceCreateRequest
import io.pleo.antaeus.models.InvoiceQuery
import io.pleo.antaeus.models.InvoiceUpdateRequest

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun update(updateRequest: InvoiceUpdateRequest) {
        dal.updateInvoice(updateRequest)
    }

    fun fetch(invoiceQuery: InvoiceQuery): List<Invoice> {
        return dal.fetchInvoices(invoiceQuery = invoiceQuery)
    }

    fun create(createRequest: InvoiceCreateRequest): Invoice {
        return dal.createInvoice(createRequest)
    }
}
