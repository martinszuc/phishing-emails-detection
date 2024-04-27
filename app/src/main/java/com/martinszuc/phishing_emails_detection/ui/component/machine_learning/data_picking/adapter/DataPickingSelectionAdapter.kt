package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.data_picking.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.EmailPackageMetadata;
import com.martinszuc.phishing_emails_detection.databinding.ItemPackageEmailCheckboxBinding;
import com.martinszuc.phishing_emails_detection.utils.StringUtils;

/**
 * Authored by matoszuc@gmail.com
 */

class DataPickingSelectionAdapter(
    private val onPackageSelected: (EmailPackageMetadata, Boolean) -> Unit
) : RecyclerView.Adapter<DataPickingSelectionAdapter.PackageViewHolder>() {

    private var items: List<EmailPackageMetadata> = emptyList()
    private var checkedStates: MutableMap<String, Boolean> = mutableMapOf()

    fun setItems(newItems: List<EmailPackageMetadata>) {
        items = newItems;
        notifyDataSetChanged();
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageViewHolder {
        val inflater = LayoutInflater.from(parent.context);
        return PackageViewHolder(
            ItemPackageEmailCheckboxBinding.inflate(inflater, parent, false)
        );
    }

    override fun onBindViewHolder(holder: PackageViewHolder, position: Int) {
        val item = items[position]; // Adjust position is not needed anymore
        with(holder.binding) {
            tvPackageName.text = item.packageName;
            tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe";
            tvCreationDate.text = StringUtils.formatTimestamp(item.creationDate);
            tvNumberOfEmails.text = "Emails: ${item.numberOfEmails}";
            checkBoxSelect.setOnCheckedChangeListener(null); // Clear existing listeners
            checkBoxSelect.isChecked = checkedStates[item.fileName] ?: false // Reset the checkbox state
            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                checkedStates[item.fileName] = isChecked
                onPackageSelected(item, isChecked);
            }
        }
    }

    override fun getItemCount(): Int = items.size; // No extra item for dual buttons

    class PackageViewHolder(val binding: ItemPackageEmailCheckboxBinding) : RecyclerView.ViewHolder(binding.root)
}