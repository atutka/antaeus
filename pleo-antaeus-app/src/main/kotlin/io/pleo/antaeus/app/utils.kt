
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.customer.Currency
import io.pleo.antaeus.models.invoice.Invoice
import io.pleo.antaeus.models.invoice.InvoiceStatus
import io.pleo.antaeus.models.invoice.Money
import org.joda.time.LocalDateTime
import java.math.BigDecimal
import kotlin.random.Random

// This will create all schemas and setup initial data
internal fun setupInitialData(dal: AntaeusDal) {
    val customers = (1..100).mapNotNull {
        dal.createCustomer(
            currency = Currency.values()[Random.nextInt(0, Currency.values().size)],
            email = "email" + Random.nextInt(0, 1000)+ "@email.com",
            phoneNumber = "48000000000",
            name = "name" + Random.nextInt(0, 1000)
        )
    }

    customers.forEach { customer ->
        (1..10).forEach {
            dal.createInvoice(
                amount = Money(
                        value = BigDecimal(Random.nextDouble(10.0, 500.0)),
                        currency = customer.currency
                ),
                customer = customer,
                status = if (it == 1) InvoiceStatus.PENDING else InvoiceStatus.PAID,
                successfulChargeDate = if (it > 1) LocalDateTime.now() else null
            )
        }
    }
}

// This is the mocked instance of the payment provider
internal fun getPaymentProvider(): PaymentProvider {
    return object : PaymentProvider {
        override fun charge(invoice: Invoice): Boolean {
                return Random.nextBoolean()
        }
    }
}