/*
    Defines mappings between database rows and Kotlin objects.
    To be used by `AntaeusDal`.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.customer.Currency
import io.pleo.antaeus.models.customer.Customer
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.Money
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toInvoice(): Invoice = Invoice(
        id = this[InvoiceTable.id],
        amount = Money(
                value = this[InvoiceTable.value],
                currency = Currency.valueOf(this[InvoiceTable.currency])
        ),
        status = InvoiceStatus.valueOf(this[InvoiceTable.status]),
        customerId = this[InvoiceTable.customerId],
        successfulChargeDate = if (this[InvoiceTable.successfulChargeDate] != null)
            this[InvoiceTable.successfulChargeDate]!!.toLocalDateTime().toString("yyyy-MM-dd HH:mm:ss")
        else null
)

fun ResultRow.toCustomer(): Customer = Customer(
        id = this[CustomerTable.id],
        name = this[CustomerTable.name],
        currency = Currency.valueOf(this[CustomerTable.currency]),
        email = this[CustomerTable.email],
        phoneNumber = this[CustomerTable.phoneNumber]
)
