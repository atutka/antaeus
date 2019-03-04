/*
    Implements the data access layer (DAL).
    This file implements the database queries used to fetch and insert rows in our database tables.

    See the `mappings` module for the conversions between database rows and Kotlin objects.
 */

package io.pleo.antaeus.data

import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Customer
import io.pleo.antaeus.models.CustomerUpdateRequest
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceCreateRequest
import io.pleo.antaeus.models.InvoiceQuery
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.InvoiceUpdateRequest
import io.pleo.antaeus.models.Money
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.joda.time.LocalDateTime

class AntaeusDal(private val db: Database) {
    fun fetchInvoice(id: Int): Invoice? {
        // transaction(db) runs the internal query as a new database transaction.
        return transaction(db) {
            // Returns the first invoice with matching id.
            InvoiceTable
                .select { InvoiceTable.id.eq(id) }
                .firstOrNull()
                ?.toInvoice()
        }
    }

    fun fetchInvoices(): List<Invoice> {
        return transaction(db) {
            InvoiceTable
                .selectAll()
                .map { it.toInvoice() }
        }
    }

    fun fetchInvoices(invoiceQuery: InvoiceQuery): List<Invoice> {
        return transaction(db) {
            val invoiceStatuses = invoiceQuery.statuses.map { it.name }
            InvoiceTable.select { InvoiceTable.status.inList(invoiceStatuses) }
                    .map { it.toInvoice() }
        }
    }

    fun updateInvoice(updateRequest: InvoiceUpdateRequest) {
        transaction(db) {
            InvoiceTable.update({ InvoiceTable.id eq updateRequest.id}) {
                if(updateRequest.status != null)
                    it[this.status] = updateRequest.status.toString()
                if(updateRequest.successfulChargeDate != null)
                    it[this.successfulChargeDate] = updateRequest.successfulChargeDate!!.toDateTime()
            }
        }
    }

    fun createInvoice(createRequest: InvoiceCreateRequest): Invoice {
        val customer = fetchCustomer(createRequest.customerId)!!
        return createInvoice(amount = createRequest.amount, customer = customer, successfulChargeDate = null)!!
    }

    fun createInvoice(amount: Money, customer: Customer, status: InvoiceStatus = InvoiceStatus.PENDING, successfulChargeDate: LocalDateTime?): Invoice? {
        val id = transaction(db) {
            // Insert the invoice and returns its new id.
            InvoiceTable
                .insert {
                    it[this.value] = amount.value
                    it[this.currency] = amount.currency.toString()
                    it[this.status] = status.toString()
                    it[this.customerId] = customer.id
                    if(successfulChargeDate != null)
                        it[this.successfulChargeDate] = successfulChargeDate.toDateTime()
                } get InvoiceTable.id
        }

        return fetchInvoice(id!!)
    }

    fun fetchCustomer(id: Int): Customer? {
        return transaction(db) {
            CustomerTable
                .select { CustomerTable.id.eq(id) }
                .firstOrNull()
                ?.toCustomer()
        }
    }

    fun fetchCustomers(): List<Customer> {
        return transaction(db) {
            CustomerTable
                .selectAll()
                .map { it.toCustomer() }
        }
    }

    fun createCustomer(name:String, currency: Currency, email: String, phoneNumber: String?): Customer? {
        val id = transaction(db) {
            // Insert the customer and return its new id.
            CustomerTable.insert {
                it[this.name] = name
                it[this.currency] = currency.toString()
                it[this.email] = email
                if(phoneNumber != null)
                    it[this.phoneNumber] = phoneNumber
            } get CustomerTable.id
        }

        return fetchCustomer(id!!)
    }

    fun updateCustomer(updateRequest: CustomerUpdateRequest) {
        transaction(db) {
            CustomerTable.update({ CustomerTable.id eq updateRequest.id}) {
                if(updateRequest.name != null)
                    it[this.name] = updateRequest.name!!
                if(updateRequest.currency != null)
                    it[this.currency] = updateRequest.currency.toString()
                if(updateRequest.email != null)
                    it[this.email] = updateRequest.email!!
                if(updateRequest.phoneNumber != null)
                    it[this.phoneNumber] = updateRequest.phoneNumber!!
            }
        }
    }
}
