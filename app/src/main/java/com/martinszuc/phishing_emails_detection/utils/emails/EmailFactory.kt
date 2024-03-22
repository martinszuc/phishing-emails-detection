package com.martinszuc.phishing_emails_detection.utils.emails

import android.util.Log
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartHeader
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Body
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Part
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Payload

/**
 * Authored by matoszuc@gmail.com
 */

/**
 * `EmailFactory` is a utility object for creating email entities from Gmail API `Message` objects or transforming them between different representations.
 */
object EmailFactory {

    // Minimal

    /**
     * Creates an `EmailMinimal` object from a Gmail API `Message`.
     * Extracts minimal information required for listing emails: ID, sender, subject, snippet (as body), and timestamp.
     *
     * @param email The Gmail API `Message` object.
     * @return An `EmailMinimal` entity.
     */
    fun createEmailMinimal(email: Message): EmailMinimal {
        return EmailMinimal(
            id = email.id,
            sender = email.payload.headers.find { it.name == "From" }?.value ?: "",
            subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
            body = email.snippet, // TODO set the body correctly
            timestamp = email.internalDate
        )
    }

    /**
     * Transforms an `EmailFull` object into an `EmailMinimal` entity.
     * Useful for operations where detailed email objects are converted to their minimal representation.
     *
     * @param emailFull The `EmailFull` entity.
     * @return An `EmailMinimal` entity.
     */
    fun createEmailMinimalFromFull(emailFull: EmailFull): EmailMinimal {
        return EmailMinimal(
            id = emailFull.id,
            sender = emailFull.payload.headers.find { it.name.equals("From", ignoreCase = true) }?.value ?: "",
            subject = emailFull.payload.headers.find { it.name.equals("Subject", ignoreCase = true) }?.value ?: "",
            body = emailFull.snippet,
            timestamp = emailFull.internalDate
        )
    }

    // Full

    /**
     * Creates an `EmailFull` entity from a Gmail API `Message`.
     * Extracts comprehensive information, including payload, headers, and body parts.
     *
     * @param email The Gmail API `Message` object.
     * @return An `EmailFull` entity or `null` if essential components are missing.
     */
    fun createEmailFull(email: Message): EmailFull? {
        val payload = email.payload ?: run {
            Log.d("EmailFactory", "Email with ID ${email.id} has null payload")
            return null
        }

        val headers = createHeaders(payload.headers)
        val parts = createParts(payload.parts)
        val payloadBody = payload.body?.let { Body(data = it.data ?: "", size = it.size) } ?: run {
            Log.d("EmailFactory", "Email with ID ${email.id} has null payload body")
            return null
        }

        val newPayload = Payload(
            partId = payload.partId,
            mimeType = payload.mimeType,
            filename = payload.filename,
            headers = headers,
            body = payloadBody,
            parts = parts
        )

        return EmailFull(
            id = email.id,
            threadId = email.threadId,
            labelIds = email.labelIds,
            snippet = email.snippet,
            historyId = email.historyId.toLong(),
            internalDate = email.internalDate,
            payload = Payload(
                partId = payload.partId,
                mimeType = payload.mimeType,
                filename = payload.filename,
                headers = headers,
                body = payloadBody,
                parts = parts
            )
        )
    }

    /**
     * Converts a list of `MessagePartHeader` objects to a list of `Header` entities.
     *
     * @param headers A list of `MessagePartHeader` objects.
     * @return A list of `Header` entities.
     */
    private fun createHeaders(headers: List<MessagePartHeader>?): List<Header> {
        return headers?.map { header ->
            Header(name = header.name, value = header.value)
        } ?: emptyList()
    }

    /**
     * Converts a list of Gmail API `MessagePart` objects to a list of `Part` entities.
     *
     * @param parts A list of `MessagePart` objects.
     * @return A list of `Part` entities or an empty list if no parts are present or they lack bodies.
     */
    private fun createParts(parts: List<MessagePart>?): List<Part> {
        return parts?.mapNotNull { part ->
            val partHeaders = createHeaders(part.headers)
            val partBody = part.body?.let { Body(data = it.data ?: "", size = it.size ?: 0) }
            if (partBody != null) {
                Part(
                    partId = part.partId,
                    mimeType = part.mimeType,
                    filename = part.filename,
                    headers = partHeaders,
                    body = partBody
                )
            } else {
                null
            }
        } ?: emptyList()
    }

    // Raw

    /**
     * Parses a raw email content string to extract a specific header's value.
     *
     * @param emailContent The raw email content as a string.
     * @param headerName The name of the header to extract.
     * @return The value of the specified header or `null` if not found.
     */
    fun parseHeader(emailContent: String, headerName: String): String? {
        val lines = emailContent.split("\r\n")
        for (line in lines) {
            if (line.startsWith(headerName + ":")) {
                return line.substringAfter(": ").trim()
            }
        }
        return null
    }
}
