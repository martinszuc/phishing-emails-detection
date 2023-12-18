package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.DialogEmailDetailsBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter.EmailFullAdapter

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsDetailsDialog(private val context: Context, private val email: EmailFull) {

    fun show() {
        val dialog = BottomSheetDialog(context)

        // Use View Binding to inflate the layout
        val dialogBinding = DialogEmailDetailsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        // Get the BottomSheetBehavior and expand the dialog fully
        val bottomSheetBehavior = BottomSheetBehavior.from(dialogBinding.root.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        // Create and set the EmailFullAdapter for the RecyclerView
        val emailFullAdapter = EmailFullAdapter()
        dialogBinding.emailsDetailsRecyclerView.adapter = emailFullAdapter

        // Set the LayoutManager for the RecyclerView
        dialogBinding.emailsDetailsRecyclerView.layoutManager = LinearLayoutManager(context)

        emailFullAdapter.emails = if (email != null) listOf(email) else listOf()
        emailFullAdapter.notifyDataSetChanged()

        dialog.show()
    }

}

