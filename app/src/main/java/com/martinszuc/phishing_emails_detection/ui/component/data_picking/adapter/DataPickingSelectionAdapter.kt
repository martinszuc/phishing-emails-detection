package com.martinszuc.phishing_emails_detection.ui.component.data_picking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.databinding.ItemAddPackageBinding
import com.martinszuc.phishing_emails_detection.databinding.ItemPackageEmailCheckboxBinding
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.ItemDualButtonDataPickingBinding
import com.martinszuc.phishing_emails_detection.utils.StringUtils

class DataPickingSelectionAdapter(
    private val onTrainingClicked: () -> Unit,
    private val onRetrainingClicked: () -> Unit,
    private val onPackageSelected: (EmailPackageMetadata, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<EmailPackageMetadata> = emptyList()

    companion object {
        private const val VIEW_TYPE_DUAL_BUTTON = 2
        private const val VIEW_TYPE_PACKAGE = 1
    }


    fun setItems(newItems: List<EmailPackageMetadata>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (position) {
        0 -> VIEW_TYPE_DUAL_BUTTON
        else -> VIEW_TYPE_PACKAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DUAL_BUTTON -> {
                DualButtonViewHolder(
                    ItemDualButtonDataPickingBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }

            VIEW_TYPE_PACKAGE -> PackageViewHolder(
                ItemPackageEmailCheckboxBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DualButtonViewHolder -> {
                holder.binding.btnTraining.setOnClickListener { onTrainingClicked() }
                holder.binding.btnRetraining.setOnClickListener { onRetrainingClicked() }
            }

            is PackageViewHolder -> {
                val item = items[position - 1] // Adjust position for extra dual item
                with(holder.binding) {
                    tvPackageName.text = item.packageName
                    tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
                    tvCreationDate.text = StringUtils.formatTimestamp(item.creationDate)
                    tvNumberOfEmails.text = "Emails: ${item.numberOfEmails}"
                    checkBoxSelect.setOnCheckedChangeListener(null) // Clear existing listeners
                    checkBoxSelect.isChecked = false // Reset the checkbox state
                    checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                        onPackageSelected(item, isChecked)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1 // Include extra dual item

    class AddViewHolder(val binding: ItemAddPackageBinding) : RecyclerView.ViewHolder(binding.root)
    class DualButtonViewHolder(val binding: ItemDualButtonDataPickingBinding) :
        RecyclerView.ViewHolder(binding.root)

    class PackageViewHolder(val binding: ItemPackageEmailCheckboxBinding) :
        RecyclerView.ViewHolder(binding.root)
}
