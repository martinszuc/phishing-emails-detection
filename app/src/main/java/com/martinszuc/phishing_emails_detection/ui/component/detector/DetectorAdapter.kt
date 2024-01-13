package com.martinszuc.phishing_emails_detection.ui.component.detector

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.detector.DetectorViewModel
import java.text.SimpleDateFormat
import java.util.*

class DetectorAdapter(private val viewModel: DetectorViewModel) :
    PagingDataAdapter<EmailMinimal, DetectorAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    private var selectedEmailId: String? = null

    inner class EmailViewHolder(val binding: ItemEmailSelectionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        Log.d("DetectorAdapter", "onCreateViewHolder")
        val binding =
            ItemEmailSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        Log.d("DetectorAdapter", "onBindViewHolder - Position: $position")
        val email = getItem(position)
        holder.binding.apply {
            email?.let {
                senderValue.text = email.sender
                subject.text = email.subject
                timestamp.text = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(email.timestamp))

                checkbox.isChecked = email.id == selectedEmailId
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    Log.d("DetectorAdapter", "Checkbox changed - Email ID: ${email.id}, Checked: $isChecked")
                    if (isChecked) {
                        selectedEmailId = email.id
                        viewModel.toggleEmailSelected(email)
                        notifyDataSetChanged()  // Refresh to ensure only one checkbox is selected
                    } else if (selectedEmailId == email.id) {
                        selectedEmailId = null
                        viewModel.toggleEmailSelected(email)
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
