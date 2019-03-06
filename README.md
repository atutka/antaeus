## General idea

Knowing that the main functionality was to be periodic collection of payments for invoices I decided to build around this
simple functionalities for invoice management. I wanted my application to be able to:
* periodically collect invoice receivables
* notification with e-mail when problem with collecting payments occurs
* invoke charging payments
* checking the invoice status
* add new invoices
* cancel invoices
* search for customers
* add/update customers
* be able to configure application from the configuration file

## Structure
I left the structure the way that it was because in my opinion it was good
```
├── pleo-antaeus-app
|
├── pleo-antaeus-core
|
├── pleo-antaeus-data
|
├── pleo-antaeus-models
|
├── pleo-antaeus-rest
└──
```

## Scheduled charging

I used the Quartz library to implement periodically charging customers for invoice because: it was easy to use,
has very good documentation and is reliable in operation. I have created a BillingJob which will be launched on the first day
each month. On the first day of the month only invoices in PENDING status are paid, all others are skipped.
I thought invoices with an error status should be checked in person to correct the error.
In the case of errors in charging, I wanted the application to automatically notify which invoices were unable to be paid.
For this it is sent e-mail from the mailbox pleoantaeus@gmail.com with information on which invoices could not be paid.
The email configuration is possible from the application.properties file

## Managing invoices

I have extended invoices for new statuses, mainly with a payment error and the status of cancellation for information
for what reason the invoice is not paid, so you can try to correct the error. I also added a field specifying when the invoice
have been paid, in my opinion it is valuable information from the business side. I added a manual call to collect dependence for the invoice
because once the payment problem no longer exists, you can re-order the payment. It is possible also to change invoice status to PENDING
and it will be automatically charged next month. 

In order for invoices to appear in the system, I have added method for adding invoice.

## Managing customers

In addition to methods to search for customers, I've added a method for adding and updating customers, because new ones may appear
and I wanted to be able to issue invoices. Updating customer data is also for improvement outdated customer data.

I have added contact fields (email, phoneNumber) to the Customer table because I thought the contact details should be
available on the application side in case it would be necessary to contact the client directly eg in a situation that is too low
account balances

## Application properties

The configuration data of the application I wanted them to be in a separate file. This helps when, for example, the password changes
to mailboxes, or invoiced charge process will be every 15 days and not once a month.

## Rest services

```
├── GET /invoices - find all invoices
|── PUT /invoices - create invoice with request:
    {
        "customerId": ?,
        "amount": {
            "value": ??,
            "currency": "??"
        }
    }
|── POST /invoices - update invoice with request:
    {
        "id": "??",
        "status": "??",
        "successfulChargeDate": "??",
    }
|── GET /invoices/id/:id - find invoice with id
|── GET /invoices/unpaid - find invoices with status unpaid
|── POST /invoices/charge/id/:id - charge invoice with id
|── POST /invoices/cancel/id/:id - set invoice to canceled
|
├── GET /customers - find all customers
|── GET /customers/id/:id - find customer with id
├── PUT /customers - create customer with request:
    {
        "name": "??",
        "currency": "??"
        "email": "??",
        "phoneNumber": "??"
    }
|── POST /customers - update customer with request:
    {
        "id": "??",
        "name": "??",
        "currency": "??",
        "email": "??",
        "phoneNumber": "??"
    }
└──
```

## How to run
If you are running application for the first time and want do receive email notification when something goes wrong with
payment please uncomment and set property "mail.supportemail" in file application.properties. Example below:
```
mail.supportemail=aaa@email.com
```
To run application:
```
./docker-start.sh
```

## Libraries in use
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Quartz](https://www.quartz-scheduler.net) - scheduler library
