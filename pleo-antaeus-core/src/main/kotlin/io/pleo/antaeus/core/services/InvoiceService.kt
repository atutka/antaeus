/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceQuery

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
       return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoices(id) ?: throw InvoiceNotFoundException(id)
    }

    fun update(invoice: Invoice) {
        dal.updateInvoice(invoice)
    }

    fun fetch(invoiceQuery: InvoiceQuery): List<Invoice> {
        return dal.fetchInvoices(invoiceQuery = invoiceQuery)
    }
}
