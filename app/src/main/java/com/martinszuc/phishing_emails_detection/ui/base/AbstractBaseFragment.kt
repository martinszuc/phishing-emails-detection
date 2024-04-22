package com.martinszuc.phishing_emails_detection.ui.base

import android.content.Context
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_class.PhishyDialogResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Authored by matoszuc@gmail.com
 */

abstract class AbstractBaseFragment : Fragment() {

    protected fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    protected suspend fun showPackageConfigDialog(context: Context): PhishyDialogResult =
        suspendCoroutine { cont ->
            val packageNameInput = EditText(context).apply {
                hint = context.getString(R.string.enter_package_name)
                inputType = InputType.TYPE_CLASS_TEXT
            }

            val isPhishyCheckbox = CheckBox(context).apply {
                text = context.getString(R.string.phishing_label_2)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                addView(packageNameInput)
                addView(isPhishyCheckbox)
            }

            MaterialAlertDialogBuilder(context).apply {
                setTitle(getString(R.string.package_config))
                setView(layout)
                setPositiveButton(getString(R.string.confirm_big)) { _, _ ->
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = isPhishyCheckbox.isChecked,
                            packageName = packageNameInput.text.toString()
                        )
                    )
                }
                setNegativeButton(getString(R.string.cancel_big)) { _, _ ->
                    cont.resume(
                        PhishyDialogResult(
                            isPhishy = false,
                            packageName = null,
                            wasCancelled = true
                        )
                    )
                }
                setCancelable(false)
            }.show()
        }
}