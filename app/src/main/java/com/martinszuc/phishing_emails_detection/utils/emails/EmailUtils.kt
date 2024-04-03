package com.martinszuc.phishing_emails_detection.utils.emails

import android.content.Context
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Authored by matoszuc@gmail.com
 */
object EmailUtils {
    fun formatToMbox(emailBlob: EmailBlob): String {
        // Format the timestamp
        val dateFormatter = SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
        dateFormatter.timeZone = TimeZone.getTimeZone("GMT+1") // Adjust to the email's timezone
        val formattedDate = dateFormatter.format(Date(emailBlob.timestamp))

        // Extract the email address from the senderEmail field
        val emailRegex = Regex("<(.+?)>")
        val matchResult = emailRegex.find(emailBlob.senderEmail)
        val emailOnly = matchResult?.groupValues?.get(1) ?: emailBlob.senderEmail

        // Build the mbox header
        val mboxHeader = "From $emailOnly $formattedDate\n"

        // Decode the email content
        val emailContent = String(emailBlob.blob)

        // Return the formatted mbox string
        return mboxHeader + emailContent + "\n"
    }

    fun mergeMboxStrings(mboxStrings: List<String>): String {
        return mboxStrings.joinToString(separator = "\n\n")
    }

    /**
     * Extracts a header value from the raw email content based on the specified header name.
     *
     * @param emailContent The raw content of the email.
     * @param headerName The name of the header to extract the value from.
     * @return The value of the specified header, or null if the header is not found.
     */
    fun extractHeaderFromEmailContent(emailContent: String, headerName: String): String? {
        val regexPattern = "$headerName: (.+)"
        val regex = Regex(regexPattern, RegexOption.IGNORE_CASE)
        val matchResult = regex.find(emailContent)
        return matchResult?.groupValues?.get(1)?.trim()
    }
}

