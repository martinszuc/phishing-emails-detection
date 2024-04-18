package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailDetection
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detection.EmailsDetectionViewModel
import com.martinszuc.phishing_emails_detection.utils.StringUtils

class EmailsDetectionAdapter(
    private val emailsDetectionViewModel: EmailsDetectionViewModel,
    private val onEmailClicked: (String) -> Unit,
    private val onAddClicked: () -> Unit
) : PagingDataAdapter<EmailDetection, RecyclerView.ViewHolder>(EMAIL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_package, parent, false)
                AddPackageViewHolder(view)
            }
            VIEW_TYPE_PACKAGE -> {
                val binding = ItemEmailDetectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EmailViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is EmailViewHolder) {
            val email = getItem(position - 1)
            if (email != null) {
                holder.binding.senderValue.text = email.emailFull.payload.headers.find { it.name == "From" }?.value
                holder.binding.subject.text = email.emailFull.payload.headers.find { it.name == "Subject" }?.value
                holder.binding.timestamp.text = StringUtils.formatTimestamp(email.emailFull.internalDate)
                holder.binding.snippet.text = email.emailFull.snippet
                holder.binding.phishingStatus.text = if (email.isPhishing) "Yes" else "No" // Assuming EmailFull has a 'isPhishy' boolean

                holder.itemView.setOnClickListener {
                    onEmailClicked(email.emailFull.id)
                }

                holder.binding.checkbox.setOnCheckedChangeListener(null)
                holder.binding.checkbox.isChecked = emailsDetectionViewModel.selectedEmails.value?.contains(email.emailFull.id) ?: false
                holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                    handleCheckboxCheckedChange(isChecked, email.emailFull)
                }
            } else {
                holder.binding.checkbox.setOnCheckedChangeListener(null)
                holder.binding.checkbox.isChecked = false
            }
        } else if (holder is AddPackageViewHolder) {
            holder.itemView.setOnClickListener {
                onAddClicked()
            }
        }
    }

    private fun handleCheckboxCheckedChange(
        isChecked: Boolean,
        email: EmailFull
    ) {
        if (isChecked) {
            emailsDetectionViewModel.toggleEmailSelected(email.id)
        } else {
            emailsDetectionViewModel.toggleEmailSelected(email.id)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_ADD else VIEW_TYPE_PACKAGE
    }

    inner class EmailViewHolder(val binding: ItemEmailDetectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class AddPackageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<EmailDetection>() {
            override fun areItemsTheSame(oldItem: EmailDetection, newItem: EmailDetection): Boolean =
                oldItem.emailFull.id == newItem.emailFull.id

            override fun areContentsTheSame(oldItem: EmailDetection, newItem: EmailDetection): Boolean =
                oldItem == newItem
        }
        private const val VIEW_TYPE_ADD = 0
        private const val VIEW_TYPE_PACKAGE = 1
    }
}
