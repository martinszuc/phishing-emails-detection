package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSelectionBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_import.EmailsGmailViewModel
import com.martinszuc.phishing_emails_detection.utils.StringUtils

/**
 * Authored by matoszuc@gmail.com
 */

class EmailsImportAdapter(private val viewModel: EmailsGmailViewModel) :
    PagingDataAdapter<EmailMinimal, EmailsImportAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

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
            holder.binding.senderValue.text = email.sender
            holder.binding.subject.text = email.subject
            holder.binding.timestamp.text = StringUtils.formatTimestamp(email.timestamp)

            // Remove the checkbox state change listener before setting the checkbox state
            holder.binding.checkbox.setOnCheckedChangeListener(null)

            // Set the checkbox state based on whether the email is selected
            holder.binding.checkbox.isChecked = email in viewModel.selectedEmails.value.orEmpty()

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckboxCheckedChange(isChecked, email)
            }

            holder.binding.checkbox.setOnLongClickListener { view ->
                handleCheckboxLongClick(view, email)
            }

        } else {
            // The item is null, indicating a placeholder item
            // Reset the checkbox state to prevent it from staying checked
            holder.binding.checkbox.setOnCheckedChangeListener(null)
            holder.binding.checkbox.isChecked = false
        }
    }

    private fun handleCheckboxCheckedChange(isChecked: Boolean, email: EmailMinimal) {
        if (isChecked) {
            viewModel.toggleEmailSelected(email)
        } else {
            viewModel.toggleEmailSelected(email)
        }
    }

    private fun handleCheckboxLongClick(view: View, email: EmailMinimal): Boolean {
        // Load and start the scale animation directly on the checkbox
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.scale_animation)
        view.startAnimation(animation)

        // Toggle selection in ViewModel
        viewModel.toggleEmailSelected(email)

        if (viewModel.isSelectionMode.value == true) {
            val snackbarMessage = view.resources.getString(R.string.ended_range_selection)
            view.showCustomSnackbar(snackbarMessage)
            // End selection mode with this email as the second selection
            viewModel.handleSecondSelection(email)
        } else {
            val snackbarMessage = view.resources.getString(R.string.started_range_selection)
            view.showCustomSnackbar(snackbarMessage)
            // Start selection mode with this email as the first selection
            val visibleEmails = snapshot().items.filterNotNull()
            viewModel.handleFirstSelection(email, visibleEmails)
        }
        return true
    }

        companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<EmailMinimal>() {
            override fun areItemsTheSame(oldItem: EmailMinimal, newItem: EmailMinimal): Boolean =
                oldItem.id == newItem.id  // Compare the IDs of the old and new items

            override fun areContentsTheSame(oldItem: EmailMinimal, newItem: EmailMinimal): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}

private fun View.showCustomSnackbar(snackbarMessage: String) {
    val snackbar = Snackbar.make(this, snackbarMessage, Snackbar.LENGTH_SHORT)
    val snackbarView = snackbar.view
    val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

    // Apply custom styles
    textView.setTextColor(ContextCompat.getColor(context, R.color.md_theme_dark_surfaceVariant))
    textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

    // Attempt to center the Snackbar manually by adjusting margins (this is a simplistic approach and might not work perfectly)
    val layoutParams = snackbarView.layoutParams
    if (layoutParams is ViewGroup.MarginLayoutParams) {
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.setMargins(
            layoutParams.leftMargin + 2, // Adjust these margins as needed
            layoutParams.topMargin,
            layoutParams.rightMargin + 50,
            layoutParams.bottomMargin + 150
        )
        snackbarView.layoutParams = layoutParams
    }

    snackbar.show()
}