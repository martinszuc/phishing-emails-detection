package com.martinszuc.phishing_emails_detection.utils.emails

import android.util.Log
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Payload
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

private const val logTag = "MboxFactory"

object MboxFactory {

    fun formatEmailFullToMbox(emailFull: EmailFull): String {
        Log.d(logTag, "Formatting EmailFull to mbox format for email ID: ${emailFull.id}")
        val headers = StringBuilder()

        // Extract and format the sender's email from the headers
        val rawFrom = emailFull.payload.headers.find { it.name.equals("From", ignoreCase = true) }?.value
            ?: "fake@example.com" // Fallback to a default if no sender is found
        val senderEmail = extractEmailFromHeader(rawFrom)

        headers.append("From $senderEmail ${formatDateForMbox(emailFull.internalDate)}\n")
        emailFull.payload.headers.forEach { header ->
            headers.append("${header.name}: ${header.value}\n")
        }
        headers.append("\n")  // Separate headers from body

        val body = formatBodyForMbox(emailFull.payload)
        val result = headers.toString() + body
        Log.d(logTag, "Formatted mbox content ready for email ID: ${emailFull.id}")
        return result
    }

    private fun formatBodyForMbox(payload: Payload): String {
        Log.d(logTag, "Formatting body for mbox. Payload has ${payload.parts?.size ?: 0} parts.")
        val bodyBuilder = StringBuilder()
        payload.parts?.forEach { part ->
            if (part.mimeType.startsWith("text/")) {
                bodyBuilder.append(part.body.data).append("\n")
            }
        } ?: payload.body.let {
            bodyBuilder.append(it.data)
        }

        val escapedBody = escapeFromLines(bodyBuilder.toString())
        Log.d(logTag, "Body formatted and escaped for mbox.")
        return escapedBody
    }

    private fun escapeFromLines(body: String): String {
        val escapedBody = body.split("\n").joinToString("\n") { line ->
            if (line.startsWith("From ")) "> $line" else line
        }
        Log.d(logTag, "Escaped 'From ' lines in mbox body.")
        return escapedBody
    }

    private fun formatDateForMbox(timeMillis: Long): String {
        val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.US)
        val formattedDate = dateFormat.format(Date(timeMillis))
        Log.d(logTag, "Formatted date for mbox: $formattedDate")
        return formattedDate
    }
    /**
     * Extracts the email address from a raw "From" header string.
     * Assumes the format could include a name and email enclosed in angle brackets.
     *
     * @param rawFrom The raw "From" header value which might contain a name and an email.
     * @return The extracted email address if present, or null if not found.
     */
    fun extractEmailFromHeader(rawFrom: String?): String? {
        // Regular expression to find an email within angle brackets
        val emailPattern = Pattern.compile("<([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})>")
        val matcher = emailPattern.matcher(rawFrom ?: "")

        // Check if the pattern matches and return the first group which is the email address
        return if (matcher.find()) matcher.group(1) else null
    }

}