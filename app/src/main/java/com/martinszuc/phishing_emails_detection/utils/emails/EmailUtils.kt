package com.martinszuc.phishing_emails_detection.utils.emails

import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailBlob
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

    fun decodeQuotedPrintable(data: String): String {
        val output = StringBuilder()
        var i = 0

        while (i < data.length) {
            when {
                data[i] == '=' -> {
                    // Handle the soft line break which ends with an '=' sign
                    if (i + 1 < data.length && data[i + 1] == '\r' && i + 2 < data.length && data[i + 2] == '\n') {
                        i += 2 // Skip the CRLF
                    } else if (i + 1 < data.length && data[i + 1] == '\n') {
                        i++ // Skip the LF (for robustness)
                    } else {
                        // Convert the next two hex digits to a byte
                        if (i + 2 < data.length) {
                            val hex = data.substring(i + 1, i + 3)
                            output.append(hex.toInt(16).toChar())
                            i += 2
                        }
                    }
                }
                else -> output.append(data[i])
            }
            i++
        }
        return output.toString()
    }
}

