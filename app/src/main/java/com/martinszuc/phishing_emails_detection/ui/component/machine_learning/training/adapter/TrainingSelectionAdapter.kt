package com.martinszuc.phishing_emails_detection.ui.component.machine_learning.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.ItemProcessedPackageBinding

/**
 * Authored by matoszuc@gmail.com
 */

class TrainingSelectionAdapter(
    private val onPackageSelected: (ProcessedPackageMetadata, Boolean) -> Unit
) : RecyclerView.Adapter<TrainingSelectionAdapter.ProcessedPackageViewHolder>() {

    private var items: List<ProcessedPackageMetadata> = emptyList()
    private var checkedStates: MutableMap<String, Boolean> = mutableMapOf()

    fun setItems(newItems: List<ProcessedPackageMetadata>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessedPackageViewHolder {
        val binding = ItemProcessedPackageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProcessedPackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProcessedPackageViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvProcessedPackageName.text = item.packageName

            // Set checkbox state based on the map or default to false if not present
            checkBoxSelectProcessed.setOnCheckedChangeListener(null) // Clear existing listeners
            checkBoxSelectProcessed.isChecked = checkedStates[item.fileName] ?: false

            // Update the map and handle the checkbox state when changed
            checkBoxSelectProcessed.setOnCheckedChangeListener { _, isChecked ->
                checkedStates[item.fileName] = isChecked
                onPackageSelected(item, isChecked)
            }

            tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
        }
    }

    override fun getItemCount(): Int = items.size

    class ProcessedPackageViewHolder(val binding: ItemProcessedPackageBinding) : RecyclerView.ViewHolder(binding.root)

    fun clearCheckedStates() {
        checkedStates.clear()
        notifyDataSetChanged()  // Notify the adapter to update all items
    }
}