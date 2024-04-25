package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailMinimal
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.EmailFull
import com.martinszuc.phishing_emails_detection.databinding.DialogEmailDetailsBinding

/**
 * Authored by matoszuc@gmail.com
 */

class EmailsDetailsDialogFragment(
    private val emailMinimal: EmailMinimal,
    private val emailFull: EmailFull
) : DialogFragment() {

    private lateinit var binding: DialogEmailDetailsBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Initialize the BottomSheetDialog with context
        val dialog = BottomSheetDialog(requireContext())

        // Inflate the layout for this dialog
        binding = DialogEmailDetailsBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        // Setup ViewPager2 with adapter
        setupViewPager()

        // Adjust the BottomSheetDialog and its contents on show
        adjustBottomSheetOnShow(dialog)

        return dialog
    }


    // Setup ViewPager2 with adapter and page change callback
    private fun setupViewPager() {
        val adapter = DetailsPagerAdapter(requireActivity(), emailMinimal, emailFull)
        binding.detailsViewPager.adapter = adapter

        // Register a callback to hide the arrow on the last page
        binding.detailsViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.swipeIndicatorArrow.visibility = if (position >= adapter.itemCount - 1) View.INVISIBLE else View.VISIBLE
            }
        })
    }

    // Adjust the BottomSheetDialog and its contents when the dialog is shown
    private fun adjustBottomSheetOnShow(dialog: BottomSheetDialog) {
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { configureBottomSheet(it) }
        }
    }

    // Configure the BottomSheet's height and expanded state
    private fun configureBottomSheet(bottomSheet: FrameLayout) {
        val desiredHeight = (Resources.getSystem().displayMetrics.heightPixels * 0.75).toInt()

        // Set the height of the BottomSheet and the ViewPager2
        bottomSheet.layoutParams = bottomSheet.layoutParams.apply { height = desiredHeight }
        binding.detailsViewPager.layoutParams = binding.detailsViewPager.layoutParams.apply { height = desiredHeight }

        bottomSheet.requestLayout() // Apply the layout changes

        // Set the BottomSheet to expanded state
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    interface DialogDismissListener {
        fun onDialogDismissed()
    }

    // Set a listener for dialog dismiss events
    private var dismissListener: DialogDismissListener? = null

    fun setDialogDismissListener(listener: DialogDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        binding.detailsViewPager.adapter = null
        super.onDismiss(dialog)
        dismissListener?.onDialogDismissed()
    }
}
