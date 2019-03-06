package io.pleo.antaeus.core.exceptions

class InvoiceCancelledException(invoiceId: Int) : Exception("Invoice with id '$invoiceId' was canceled and cannot by charged")