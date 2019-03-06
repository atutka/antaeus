package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifyAll
import org.junit.jupiter.api.Test
import java.util.*
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage

class NotificationServiceTest {

    private val properties = mockk<Properties>()
    private val emailService = mockk<EmailService>()

    private val notificationService = NotificationService(properties, emailService)

    @Test
    fun `will not send because of no email set`()
    {
        val invoiceIds = mockk<List<Int>>()
        every { properties.getProperty(eq(PROPERTY_SUPPORT_EMAIL)) } returns null

        notificationService.sendNotification(invoiceIds)

        verifyAll {
            properties.getProperty(PROPERTY_SUPPORT_EMAIL)
        }
    }

    @Test
    fun `will throw messaging exception`()
    {
        val invoiceIds = listOf(100)
        val recipient = "recipient"
        val subject = "subject"
        val messageFormat = "messageFormat"
        val messageText = mockk<MimeMessage>()
        val exception = MessagingException()
        every { properties.getProperty(eq(PROPERTY_SUPPORT_EMAIL)) } returns recipient
        every { properties.getProperty(eq(PROPERTY_MAIL_SUBJECT)) } returns subject
        every { properties.getProperty(eq(PROPERTY_MAIL_TEXT)) } returns messageFormat
        every { emailService.createMessage(eq(recipient), eq(subject), eq(messageFormat)) } returns messageText
        every { emailService.sendEmail(refEq(messageText)) } throws exception

        notificationService.sendNotification(invoiceIds)

        verifyAll {
            properties.getProperty(PROPERTY_SUPPORT_EMAIL)
            properties.getProperty(eq(PROPERTY_MAIL_SUBJECT))
            properties.getProperty(eq(PROPERTY_MAIL_TEXT))
            emailService.createMessage(eq(recipient), eq(subject), eq(messageFormat))
            emailService.sendEmail(refEq(messageText))
        }
    }

    @Test
    fun `will send notification`()
    {
        val invoiceIds = listOf(100)
        val recipient = "recipient"
        val subject = "subject"
        val messageFormat = "messageFormat"
        val messageText = mockk<MimeMessage>()
        every { properties.getProperty(eq(PROPERTY_SUPPORT_EMAIL)) } returns recipient
        every { properties.getProperty(eq(PROPERTY_MAIL_SUBJECT)) } returns subject
        every { properties.getProperty(eq(PROPERTY_MAIL_TEXT)) } returns messageFormat
        every { emailService.createMessage(eq(recipient), eq(subject), eq(messageFormat)) } returns messageText
        every { emailService.sendEmail(refEq(messageText)) } just runs

        notificationService.sendNotification(invoiceIds)

        verifyAll {
            properties.getProperty(PROPERTY_SUPPORT_EMAIL)
            properties.getProperty(eq(PROPERTY_MAIL_SUBJECT))
            properties.getProperty(eq(PROPERTY_MAIL_TEXT))
            emailService.createMessage(eq(recipient), eq(subject), eq(messageFormat))
            emailService.sendEmail(refEq(messageText))
        }
    }
}