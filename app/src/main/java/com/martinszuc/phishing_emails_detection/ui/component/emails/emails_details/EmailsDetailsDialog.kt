package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.martinszuc.phishing_emails_detection.data.email.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.DialogEmailDetailsBinding

/**
 * Authored by matoszuc@gmail.com
 */
class EmailsDetailsDialog(
    private val emailMinimal: EmailMinimal,
    private val emailFull: EmailFull
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogEmailDetailsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)

        val bottomSheetBehavior = BottomSheetBehavior.from(dialogBinding.root.parent as View)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        val adapter = DetailsPagerAdapter(requireActivity(), emailMinimal, emailFull)
        dialogBinding.detailsViewPager.adapter = adapter

        return dialog
    }

    interface DialogDismissListener {
        fun onDialogDismissed()
    }

    // Add a property for the listener
    private var dismissListener: DialogDismissListener? = null

    fun setDialogDismissListener(listener: DialogDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDialogDismissed()
    }
}
