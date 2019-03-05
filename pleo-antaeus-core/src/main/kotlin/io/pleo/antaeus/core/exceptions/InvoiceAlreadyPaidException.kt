package io.pleo.antaeus.core.exceptions

class InvoiceAlreadyPaidException(invoiceId: Int) : Exception("Invoice with id '$invoiceId' was already paid")