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

object EmailFactory {



    // Minimal
    fun createEmailMinimal(email: Message): EmailMinimal {
        return EmailMinimal(
            id = email.id,
            sender = email.payload.headers.find { it.name == "From" }?.value ?: "",
            subject = email.payload.headers.find { it.name == "Subject" }?.value ?: "",
            body = email.snippet, // TODO set the body correctly
            timestamp = email.internalDate
        )
    }

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
            payload = newPayload
        )
    }

    private fun createHeaders(headers: List<MessagePartHeader>?): List<Header> {
        return headers?.map { header ->
            Header(name = header.name, value = header.value)
        } ?: emptyList()
    }

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
