package com.martinszuc.phishing_emails_detection.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.models.Email
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.viewmodels.EmailViewModel

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

class EmailAdapter(private val viewModel: EmailViewModel) : PagingDataAdapter<Email, EmailAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    // TODO fix what to do with selected emails
    inner class EmailViewHolder(val binding: ItemEmailSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding = ItemEmailSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        if (email != null) {
            holder.binding.sender.text = email.from
            holder.binding.subject.text = email.subject
            holder.binding.checkbox.isChecked = email.isSelected

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                email.isSelected = isChecked
                viewModel.toggleEmailSelected(email)
            }
        }
    }

    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<Email>() {
            override fun areItemsTheSame(oldItem: Email, newItem: Email): Boolean =
                oldItem.from == newItem.from &&
                        oldItem.subject == newItem.subject &&
                        oldItem.body == newItem.body

            override fun areContentsTheSame(oldItem: Email, newItem: Email): Boolean =
                oldItem == newItem
        }
    }
}
