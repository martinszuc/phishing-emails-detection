package com.martinszuc.phishing_emails_detection.ui.component.data_picking.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.databinding.ItemAddPackageBinding
import com.martinszuc.phishing_emails_detection.databinding.ItemPackageEmailCheckboxBinding
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.StringUtils

class DataPickingSelectionAdapter(
    private val onAddClicked: () -> Unit,
    private val onPackageSelected: (EmailPackageMetadata, Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<EmailPackageMetadata> = emptyList()

    companion object {
        private const val VIEW_TYPE_ADD = 0
        private const val VIEW_TYPE_PACKAGE = 1
    }

    fun setItems(newItems: List<EmailPackageMetadata>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = if (position == 0) VIEW_TYPE_ADD else VIEW_TYPE_PACKAGE

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> AddViewHolder(ItemAddPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            VIEW_TYPE_PACKAGE -> PackageViewHolder(ItemPackageEmailCheckboxBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddViewHolder -> holder.binding.root.setOnClickListener { onAddClicked() }
            is PackageViewHolder -> {
                val item = items[position - 1] // Adjust position for add item
                with(holder.binding) {
                    tvPackageName.text = item.packageName
                    tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
// Assuming you have a method StringUtils.formatTimestamp for date formatting
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

    override fun getItemCount(): Int = items.size + 1 // Include add item

    class AddViewHolder(val binding: ItemAddPackageBinding) : RecyclerView.ViewHolder(binding.root)

    class PackageViewHolder(val binding: ItemPackageEmailCheckboxBinding) : RecyclerView.ViewHolder(binding.root)
}
