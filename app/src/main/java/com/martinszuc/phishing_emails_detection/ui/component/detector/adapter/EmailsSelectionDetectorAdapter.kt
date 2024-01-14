package com.martinszuc.phishing_emails_detection.ui.component.detector.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionDetectorBinding
import com.martinszuc.phishing_emails_detection.ui.component.detector.DetectorViewModel

class EmailsSelectionDetectorAdapter(private val viewModel: DetectorViewModel) :
    PagingDataAdapter<EmailMinimal, EmailsSelectionDetectorAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    inner class EmailViewHolder(val binding: ItemEmailSelectionDetectorBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        Log.d("DetectorAdapter", "onCreateViewHolder")
        val binding =
            ItemEmailSelectionDetectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        Log.d("DetectorAdapter", "onBindViewHolder - Position: $position")
        val email = getItem(position)
        holder.binding.apply {
            email?.let {
                senderValue.text = email.sender
                subject.text = email.subject
                bodyText.text = email.body

                checkbox.setOnCheckedChangeListener(null)

                val adapterPosition = holder.adapterPosition
                val isSelected = email.id == viewModel.selectedEmailId.value

                checkbox.isChecked = isSelected

                checkbox.setOnClickListener {
                    if (!isSelected) {
                        viewModel.toggleEmailSelected(email.id)
                        notifyItemChanged(adapterPosition) // To refresh the entire list and update checkboxes
                    }
                }
            }
        }
    }

    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<EmailMinimal>() {
            override fun areItemsTheSame(oldItem: EmailMinimal, newItem: EmailMinimal): Boolean {
                Log.d("DetectorAdapter", "areItemsTheSame - Old ID: ${oldItem.id}, New ID: ${newItem.id}")
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EmailMinimal, newItem: EmailMinimal): Boolean {
                Log.d("DetectorAdapter", "areContentsTheSame - Old Item: $oldItem, New Item: $newItem")
                return oldItem == newItem
            }
        }
    }
}
