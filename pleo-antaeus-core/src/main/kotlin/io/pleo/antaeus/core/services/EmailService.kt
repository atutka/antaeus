package io.pleo.antaeus.core.services

import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

internal const val HEADER_NAME = "XPriority"
internal const val HEADER_VALUE = "1"

internal const val PROPERTY_LOGIN = "mail.login"
internal const val PROPERTY_PASSWORD = "mail.password"

class EmailService(
        private val currentDateTimeService: CurrentDateTimeService,
        private val properties: Properties
) {

    fun sendEmail(message: Message) {
        Transport.send(message)
    }

    fun createMessage(recipient: String, subject: String, messageText: String): Message {
        val session = getSession()
        val message = MimeMessage(session)
        val address = InternetAddress.parse(recipient, true)
        message.setRecipients(Message.RecipientType.TO, address)
        message.subject = subject
        message.sentDate = currentDateTimeService.getCurrentLocalDateTime().toDate()

        message.setText(messageText)
        message.setHeader(HEADER_NAME, HEADER_VALUE)
        return message
    }

    private fun getSession(): Session {
        return Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication? {
                return PasswordAuthentication(properties.getProperty(PROPERTY_LOGIN), properties.getProperty(PROPERTY_PASSWORD))
            }
        })
    }
}