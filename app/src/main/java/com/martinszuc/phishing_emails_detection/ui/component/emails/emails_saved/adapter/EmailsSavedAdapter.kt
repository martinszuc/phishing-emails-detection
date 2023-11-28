package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsSavedAdapter(private val viewModel: EmailsSavedViewModel) :
    PagingDataAdapter<Email, EmailsSavedAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    inner class EmailViewHolder(val binding: ItemEmailSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding =
            ItemEmailSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        Log.d("EmailsSavedAdapter", "Binding email at position $position")
        if (email != null) {
            // Convert the timestamp to a human-readable format
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = Date(email.timestamp)

            holder.binding.sender.text = email.sender
            holder.binding.subject.text = email.subject
            holder.binding.timestamp.text = sdf.format(date)
        }
    }

    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<Email>() {
            override fun areItemsTheSame(oldItem: Email, newItem: Email): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Email, newItem: Email): Boolean =
                oldItem == newItem
        }
    }
}