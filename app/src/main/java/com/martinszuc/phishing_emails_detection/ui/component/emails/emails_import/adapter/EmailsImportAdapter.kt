package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.Email
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailViewModel

import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil

class EmailsImportAdapter(private val viewModel: EmailViewModel) :
    PagingDataAdapter<Email, EmailsImportAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    // TODO selected emails => store to db
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
            holder.binding.sender.text = email.from
            holder.binding.subject.text = email.subject
            holder.binding.checkbox.isChecked = false

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewModel.toggleEmailSelected(email)
                } else {
                    viewModel.toggleEmailSelected(email)
                }
            }
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
