package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.verifyAll
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*
import javax.mail.Message
import javax.mail.Transport
import javax.mail.internet.MimeMessage

class EmailServiceTest {

    private val currentDateTimeService = mockk<CurrentDateTimeService>()
    private val properties = mockk<Properties>()

    private val emailService = EmailService(currentDateTimeService, properties)

    @Test
    fun `will send email`() {
        mockkStatic(Transport::class)
        val message = mockk<MimeMessage>()

        every { Transport.send(refEq(message)) } just runs

        emailService.sendEmail(message)

        verifyAll {
            Transport.send(message)
        }
    }

    @Test
    fun `will create message`() {
        val recipient = "recipient"
        val subject = "subject"
        val messageText = "messageText"
        val localDateTime = LocalDateTime(2019, 6, 3, 17, 0 ,0)
        every { currentDateTimeService.getCurrentLocalDateTime() } returns localDateTime
        every { properties.getProperty(any()) } returns "false"
        every { properties.get(any()) } returns null

        val result = emailService.createMessage(recipient, subject, messageText)

        assertEquals(1, result.getRecipients(Message.RecipientType.TO).size)
        assertEquals(recipient, result.getRecipients(Message.RecipientType.TO).first().toString())
        assertEquals(subject, result.subject)
        assertTrue(result is MimeMessage)
        assertEquals(messageText, (result as MimeMessage).content.toString())


        verifyAll {
            currentDateTimeService.getCurrentLocalDateTime()
        }
    }
}