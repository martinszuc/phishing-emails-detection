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

    fun saveMboxToFile(context: Context, mboxContent: String, fileName: String = "emails.mbox"): File {
        val file = File(context.filesDir, fileName)
        file.writeText(mboxContent)
        return file
    }

    fun exportEmailsToFile(context: Context, emailBlobs: List<EmailBlob>, fileName: String = "exportedEmails.mbox"): File {
        // Convert each EmailBlob to an mbox string
        val mboxStrings = emailBlobs.map { formatToMbox(it) }

        // Merge the mbox strings into a single string
        val mergedMbox = mergeMboxStrings(mboxStrings)

        // Save the merged mbox string to a file and return the File object
        return saveMboxToFile(context, mergedMbox, fileName)
    }
}