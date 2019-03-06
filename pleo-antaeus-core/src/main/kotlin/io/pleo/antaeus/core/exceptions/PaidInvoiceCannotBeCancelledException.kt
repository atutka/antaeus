package io.pleo.antaeus.core.exceptions

class PaidInvoiceCannotBeCancelledException(invoiceId: Int) : Exception("Invoice with id '$invoiceId' was paid and cannot by cancelled")