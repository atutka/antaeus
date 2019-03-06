package io.pleo.antaeus.core.services

import mu.KotlinLogging
import java.util.*
import javax.mail.MessagingException

private val logger = KotlinLogging.logger {}

internal const val PROPERTY_SUPPORT_EMAIL = "mail.supportemail"
internal const val PROPERTY_MAIL_SUBJECT = "mail.subject"
internal const val PROPERTY_MAIL_TEXT = "mail.text"

class NotificationService(
        private val properties: Properties,
        private val emailService: EmailService
) {

    fun sendNotification(invoiceIds: List<Int>) {
        try {
            val recipient = properties.getProperty(PROPERTY_SUPPORT_EMAIL)
            if(recipient == null) {
                logger.info("Notification will not be sent because there is not support email set")
                return
            }
            val subject = properties.getProperty(PROPERTY_MAIL_SUBJECT)
            val messageText = String.format(properties.getProperty(PROPERTY_MAIL_TEXT), invoiceIds)
            val message = emailService.createMessage(recipient, subject, messageText)
            emailService.sendEmail(message)
            logger.info("Mail has been sent successfully")
        } catch (mex: MessagingException) {
            logger.error("Unable to send an email", mex)
        }

    }

}