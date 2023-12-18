package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSavedBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.EmailsDetailsDialog
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsSavedAdapter(private val viewModel: EmailsSavedViewModel) :
    PagingDataAdapter<EmailFull, EmailsSavedAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    inner class EmailViewHolder(val binding: ItemEmailSavedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding =
            ItemEmailSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        Log.d("EmailsSavedAdapter", "Binding email at position $position")
        if (email != null) {
            // Convert the timestamp to a human-readable format
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = Date(email.internalDate)

            holder.binding.sender.text = email.payload.headers.find { it.name == "From" }?.value
            holder.binding.subject.text = email.payload.headers.find { it.name == "Subject" }?.value
            holder.binding.timestamp.text = sdf.format(date)
            holder.binding.snippet.text = email.snippet

            // Check if parts is not null or empty
            if (!email.payload.parts.isNullOrEmpty()) {
                holder.binding.attachments.text = email.payload.parts.size.toString()
                holder.binding.attachments.visibility = View.VISIBLE
            } else {
                holder.binding.attachments.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                EmailsDetailsDialog(it.context, email).show()
            }
        }
    }


    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<EmailFull>() {
            override fun areItemsTheSame(oldItem: EmailFull, newItem: EmailFull): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EmailFull, newItem: EmailFull): Boolean =
                oldItem == newItem
        }
    }
}
