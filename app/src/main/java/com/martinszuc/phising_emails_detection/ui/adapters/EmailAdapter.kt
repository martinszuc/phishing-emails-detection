package com.martinszuc.phising_emails_detection.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phising_emails_detection.data.models.Email
import com.martinszuc.phising_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phising_emails_detection.ui.viewmodels.EmailViewModel

class EmailAdapter(private var emails: List<Email>, private val viewModel: EmailViewModel) : RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

    // TODO fix what to do with selected emails
    inner class EmailViewHolder(val binding: ItemEmailSelectionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding = ItemEmailSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = emails[position]
        holder.binding.sender.text = email.from
        holder.binding.subject.text = email.subject
        holder.binding.checkbox.isChecked = email.isSelected

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            email.isSelected = isChecked
            viewModel.toggleEmailSelected(email)
        }
    }

    override fun getItemCount() = emails.size

    fun updateEmails(newEmails: List<Email>) {
        emails = newEmails
        notifyDataSetChanged()
    }
}
