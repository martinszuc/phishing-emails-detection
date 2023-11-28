package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailsImportViewModel

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmailsImportAdapter(private val viewModel: EmailsImportViewModel) :
    PagingDataAdapter<Email, EmailsImportAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    inner class EmailViewHolder(val binding: ItemEmailSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding =
            ItemEmailSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        Log.d("EmailAdapter", "Binding email at position $position")
        if (email != null) {
            // Convert the timestamp to a human-readable format
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = Date(email.timestamp)

            holder.binding.sender.text = email.sender
            holder.binding.subject.text = email.subject
            holder.binding.timestamp.text = sdf.format(date)

            // Remove the checkbox state change listener before setting the checkbox state
            holder.binding.checkbox.setOnCheckedChangeListener(null)

            // Set the checkbox state based on whether the email is selected
            holder.binding.checkbox.isChecked = email in viewModel.selectedEmails.value.orEmpty()

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.toggleEmailSelected(email)
                } else {
                    viewModel.toggleEmailSelected(email)
                }
            }
        } else {
            // The item is null, indicating a placeholder item
            // Reset the checkbox state to prevent it from staying checked
            holder.binding.checkbox.setOnCheckedChangeListener(null)
            holder.binding.checkbox.isChecked = false
        }
    }




    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<Email>() {
            override fun areItemsTheSame(oldItem: Email, newItem: Email): Boolean =
                oldItem.id == newItem.id  // Compare the IDs of the old and new items

            override fun areContentsTheSame(oldItem: Email, newItem: Email): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}
