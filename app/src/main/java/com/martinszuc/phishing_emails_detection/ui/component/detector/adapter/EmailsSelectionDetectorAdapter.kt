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
/**
 * Authored by matoszuc@gmail.com
 */
class EmailsSelectionDetectorAdapter(
    private val viewModel: DetectorViewModel,
    lifecycleOwner: LifecycleOwner,
    private val emailSelectionDetectorItemListener: EmailSelectionDetectorItemListener
) : PagingDataAdapter<EmailMinimal, EmailsSelectionDetectorAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    private var currentlySelectedId: String? = null
    private var deselectingDueToNewSelection = false


    init {
        viewModel.selectedEmailId.observe(lifecycleOwner) { selectedId ->
            if (currentlySelectedId != null && selectedId != currentlySelectedId) {
                deselectingDueToNewSelection = true
                notifyItemChangedForId(currentlySelectedId)
            }
            notifyItemChangedForId(selectedId)
            currentlySelectedId = selectedId
            deselectingDueToNewSelection = false
        }
    }

    // Helper method to notify change for a specific ID
    private fun notifyItemChangedForId(id: String?) {
        id ?: return
        val index = snapshot().indexOfFirst { it?.id == id }
        if (index != -1) notifyItemChanged(index)
    }

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

                val isSelected = email.id == viewModel.selectedEmailId.value
                checkbox.isChecked = isSelected

                checkbox.setOnClickListener {
                    handleSelection(holder, email, isSelected)
                }
            }
        }
        holder.binding.root.setOnClickListener {
            email?.let { emailItem ->
                emailSelectionDetectorItemListener.onEmailClicked(emailItem.id)
            }
        }
    }

    private fun handleSelection(holder: EmailViewHolder, email: EmailMinimal, isSelected: Boolean) {
        val currentPos = holder.bindingAdapterPosition
        if (currentPos != RecyclerView.NO_POSITION) {
            if (!isSelected) {
                // Temporarily disable the click listener to avoid visual feedback during automatic deselection
                if (deselectingDueToNewSelection) {
                    holder.binding.root.isClickable = false
                }

                viewModel.toggleEmailSelected(email.id)

                // Re-enable the click listener
                holder.binding.root.isClickable = true
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
