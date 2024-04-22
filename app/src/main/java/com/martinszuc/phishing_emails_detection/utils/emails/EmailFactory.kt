package com.martinszuc.phishing_emails_detection.utils.emails

import android.util.Base64
import android.util.Log
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.google.api.services.gmail.model.MessagePartHeader
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.Body
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.Part
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.utils.Constants
import com.martinszuc.phishing_emails_detection.utils.StringUtils
import java.nio.charset.StandardCharsets
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

private const val logTag = "EmailFactory"

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
    private fun decodeBase64UrlSafe(base64Data: String?): String? {
        if (base64Data == null) {
            Log.e("EmailFactory", "Attempted to decode null Base64 data")
            return null
        }
        return try {
            val data = Base64.decode(base64Data, Base64.URL_SAFE or Base64.NO_WRAP)
            String(data, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            Log.e("EmailFactory", "Base64 decoding error: ${e.message}")
            null
        }
    }

    fun createEmailFull(email: Message): EmailFull? {
        val payload = email.payload ?: return null
        val headers = createHeaders(payload.headers)
        val parts = createParts(payload.parts ?: listOf())

        val payloadBody = payload.body?.data?.let {
            decodeBase64UrlSafe(it)
        }?.let {
            Body(data = it, size = payload.body.size)
        } ?: Body(data = "", size = 0)

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
                body = payloadBody,  // Can be null if body data was null
                parts = if (parts.isEmpty()) null else parts
            ),
        )
    }

    private fun createHeaders(headers: List<MessagePartHeader>): List<Header> {
        return headers.map { Header(name = it.name, value = it.value ?: "") }
    }

    private fun createParts(parts: List<MessagePart>): List<Part> {
        return parts.mapNotNull { part ->
            val partHeaders = createHeaders(part.headers)
            val partBody = part.body?.data?.let {
                decodeBase64UrlSafe(it)
            }?.let {
                Body(data = it, size = part.body.size)
            }
            if (partBody != null) {
                Part(
                    partId = part.partId,
                    mimeType = part.mimeType,
                    filename = part.filename,
                    headers = partHeaders,
                    body = partBody
                )
            } else null
        }
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

    fun parseEmlToEmailFull(content: String): EmailFull {
        val lines = content.split("\n")
        val headers = mutableMapOf<String, String>()
        var body = StringBuilder()
        var readingHeaders = true

        for (line in lines) {
            if (line.trim().isEmpty() && readingHeaders) {
                readingHeaders = false
            } else if (readingHeaders) {
                val headerParts = line.split(":", limit = 2)
                if (headerParts.size == 2) {
                    headers[headerParts[0].trim()] = headerParts[1].trim()
                }
            } else {
                body.append(line + "\n")
            }
        }

        val decodedBody = decodeBase64UrlSafe(body.toString()) ?: body.toString()
        val snippet = if (decodedBody.length > 50) decodedBody.substring(0, 50) else decodedBody

        return EmailFull(
            id = headers["Message-ID"] ?: StringUtils.generateClientId(),
            threadId = headers["Thread-Index"] ?: "",
            labelIds = listOf(headers["Labels"] ?: "UNLABELED"),
            snippet = snippet,
            historyId = 0,
            internalDate = headers["Date"]?.let { parseDateToMillis(it) } ?: System.currentTimeMillis(),
            payload = Payload(
                partId = null,
                mimeType = Constants.TEXT_PLAIN_TYPE,
                filename = "",
                headers = headers.map { Header(it.key, it.value) },
                body = Body(data = decodedBody, size = decodedBody.length),
                parts = null
            )
        )
    }

    private fun parseDateToMillis(dateStr: String): Long {
        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        try {
            return format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: ParseException) {
            Log.e(logTag, "Error parsing date: $dateStr", e)
            return System.currentTimeMillis()
        }
    }
}
