package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.adapter

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
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailSavedBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_saved.EmailsSavedViewModel
import com.martinszuc.phishing_emails_detection.utils.StringUtils

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsSavedAdapter(
    private val emailsSavedViewModel: EmailsSavedViewModel,
    private val onEmailClicked: (String) -> Unit
) : PagingDataAdapter<EmailFull, EmailsSavedAdapter.EmailViewHolder>(EMAIL_COMPARATOR) {

    inner class EmailViewHolder(val binding: ItemEmailSavedBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
        val binding =
            ItemEmailSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EmailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {
        val email = getItem(position)
        Log.d("EmailsSavedAdapter", "Binding email at position $position")
        if (email != null) {
            holder.binding.senderValue.text = email.payload.headers.find { it.name == "From" }?.value
            holder.binding.subject.text = email.payload.headers.find { it.name == "Subject" }?.value
            holder.binding.timestamp.text = StringUtils.formatTimestamp(email.internalDate)
            holder.binding.snippet.text = email.snippet

            if (!email.payload.parts.isNullOrEmpty()) {
                holder.binding.attachmentsValue.text = email.payload.parts.size.toString()
                holder.binding.attachmentsValue.visibility = View.VISIBLE
                holder.binding.attachmentsLabel.visibility = View.VISIBLE

            } else {
                holder.binding.attachmentsLabel.visibility = View.GONE
                holder.binding.attachmentsValue.visibility = View.GONE

            }

            holder.itemView.setOnClickListener {
                onEmailClicked(email.id)
            }

            // Checkboxes codes

            // Remove the checkbox state change listener before setting the checkbox state
            holder.binding.checkbox.setOnCheckedChangeListener(null)

            // Set the checkbox state based on whether the email ID is selected
            holder.binding.checkbox.isChecked = emailsSavedViewModel.selectedEmails.value?.contains(email.id) ?: false

            holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                handleCheckboxCheckedChange(isChecked, email)
            }

            holder.binding.checkbox.setOnLongClickListener { view ->
                handleCheckboxLongClick(view, email.id)
                true
            }
        } else {
            holder.binding.checkbox.setOnCheckedChangeListener(null)
            holder.binding.checkbox.isChecked = false
        }
    }

    private fun handleCheckboxCheckedChange(
        isChecked: Boolean,
        email: EmailFull
    ) {
        if (isChecked) {
            emailsSavedViewModel.toggleEmailSelected(email.id)
        } else {
            emailsSavedViewModel.toggleEmailSelected(email.id)
        }
    }

    private fun handleCheckboxLongClick(view: View, emailId: String): Boolean {
        // Load and start the scale animation directly on the checkbox
        val animation = AnimationUtils.loadAnimation(view.context, R.anim.scale_animation)
        view.startAnimation(animation)

        // Toggle selection in ViewModel
        emailsSavedViewModel.toggleEmailSelected(emailId)

        if (emailsSavedViewModel.isSelectionMode.value == true) {
            val snackbarMessage = view.resources.getString(R.string.ended_range_selection)
            view.showCustomSnackbar(snackbarMessage)
            // End selection mode with this email as the second selection
            emailsSavedViewModel.handleSecondSelection(emailId)
        } else {
            val snackbarMessage = view.resources.getString(R.string.started_range_selection)
            view.showCustomSnackbar(snackbarMessage)
            // Start selection mode with this email as the first selection
            val visibleEmailIds = snapshot().items.filterNotNull().map { it.id } // Map to list of IDs
            emailsSavedViewModel.handleFirstSelection(emailId, visibleEmailIds)
        }
        return true
    }


    companion object {
        private val EMAIL_COMPARATOR = object : DiffUtil.ItemCallback<EmailFull>() {
            override fun areItemsTheSame(oldItem: EmailFull, newItem: EmailFull): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: EmailFull, newItem: EmailFull): Boolean =
                oldItem == newItem
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