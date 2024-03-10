package com.martinszuc.phishing_emails_detection.ui.component.training.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.processed_packages.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.ItemProcessedPackageBinding

class TrainingSelectionAdapter(
    private val onPackageSelected: (ProcessedPackageMetadata, Boolean) -> Unit
) : RecyclerView.Adapter<TrainingSelectionAdapter.ProcessedPackageViewHolder>() {

    private var items: List<ProcessedPackageMetadata> = emptyList()

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
            checkBoxSelectProcessed.setOnCheckedChangeListener(null) // Clear existing listeners
            checkBoxSelectProcessed.isChecked = false // Reset the checkbox state
            checkBoxSelectProcessed.setOnCheckedChangeListener { _, isChecked ->
                onPackageSelected(item, isChecked)
            }
            tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
        }
    }

    override fun getItemCount(): Int = items.size

    class ProcessedPackageViewHolder(val binding: ItemProcessedPackageBinding) : RecyclerView.ViewHolder(binding.root)
}