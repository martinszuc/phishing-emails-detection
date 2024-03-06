package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSavedBinding
import com.martinszuc.phishing_emails_detection.utils.StringUtils

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsSavedAdapter(
    private val onEmailClicked: (String) -> Unit
) : PagingDataAdapter<EmailFull, EmailsSavedAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

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
            holder.binding.senderValue.text = email.payload.headers.find { it.name == "From" }?.value
            holder.binding.subject.text = email.payload.headers.find { it.name == "Subject" }?.value
            holder.binding.timestamp.text = StringUtils.formatTimestamp(email.internalDate)
            holder.binding.snippet.text = email.snippet

            if (!email.payload.parts.isNullOrEmpty()) {
                holder.binding.attachmentsValue.text = email.payload.parts.size.toString()
                holder.binding.attachmentsValue.visibility = View.VISIBLE
                holder.binding.attachmentsLabel.visibility = View.VISIBLE

            } else {
                holder.binding.attachmentsLabel.visibility = View.GONE
                holder.binding.attachmentsValue.visibility = View.GONE

            }

            holder.itemView.setOnClickListener {
                email?.let {// TODO warning "Unnecessary safe call on a non-null receiver of type EmailFull?"
                    onEmailClicked(it.id) // Assuming email has an id field
                }
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
