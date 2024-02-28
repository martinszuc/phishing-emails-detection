package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsFullBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


/**
 * Authored by matoszuc@gmail.com
 */
class EmailFullAdapter : RecyclerView.Adapter<EmailFullAdapter.EmailViewHolder>() {
    var emails: List<EmailFull> = listOf()

    inner class EmailViewHolder(val binding: ItemEmailDetailsFullBinding) : RecyclerView.ViewHolder(binding.root) {
        val payloadAdapter = PayloadAdapter().apply {
            binding.payloadRecyclerView.layoutManager = LinearLayoutManager(binding.root.context)
            binding.payloadRecyclerView.adapter = this
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding = ItemEmailDetailsFullBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding).apply {
            binding.payloadRecyclerView.adapter = payloadAdapter
        }
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = emails[position]
        holder.binding.idValue.text = email.id
        holder.binding.threadIdValue.text = email.threadId
        holder.binding.snippetValue.text = email.snippet
        // Format the historyId
        holder.binding.historyIdValue.text = String.format(Locale.getDefault(), "%,d", email.historyId)

        // Format the internalDate
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()  // Set your desired timezone here
        val formattedDate = sdf.format(email.internalDate)
        holder.binding.internalDateValue.text = formattedDate

        holder.binding.labelIdsValue.text = email.labelIds.joinToString(", ")
        holder.binding.labelIdsValue.text = email.labelIds.joinToString(", ")

        holder.payloadAdapter.payloads = listOf(email.payload)
        holder.payloadAdapter.notifyDataSetChanged()
    }

    override fun getItemCount() = emails.size
}
