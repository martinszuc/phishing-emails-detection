package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsFullBinding


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
        holder.binding.id.text = "ID: ${email.id}"
        holder.binding.threadId.text = "Thread ID: ${email.threadId}"
        holder.binding.snippet.text = "Snippet: ${email.snippet}"
        holder.binding.historyId.text = "History ID: ${email.historyId}"
        holder.binding.internalDate.text = "Internal Date: ${email.internalDate}"
        holder.binding.labelIds.text = "Label IDs: ${email.labelIds.joinToString(", ")}"

        holder.payloadAdapter.payloads = listOf(email.payload)
        holder.payloadAdapter.notifyDataSetChanged()
    }

    override fun getItemCount() = emails.size
}
