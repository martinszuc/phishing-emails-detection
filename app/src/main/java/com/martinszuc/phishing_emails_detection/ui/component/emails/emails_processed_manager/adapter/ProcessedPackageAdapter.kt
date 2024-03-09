package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.email_package.entity.ProcessedPackageMetadata
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailProcessedPackageBinding
import com.martinszuc.phishing_emails_detection.ui.component.emails.emails_processed_manager.ProcessedPackageManagerViewModel

class ProcessedPackageAdapter(
    private var packages: List<ProcessedPackageMetadata>,
    private val onDeleteClicked: (String) -> Unit,
    private val onAddClicked: (View) -> Unit
) : RecyclerView.Adapter<ProcessedPackageAdapter.ProcessedPackageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProcessedPackageViewHolder {
        val binding = ItemEmailProcessedPackageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProcessedPackageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProcessedPackageViewHolder, position: Int) {
        val processedPackage = packages[position]
        holder.bind(processedPackage)
    }

    override fun getItemCount(): Int = packages.size

    fun setItems(newPackages: List<ProcessedPackageMetadata>) {
        packages = newPackages
        notifyDataSetChanged()
    }

    inner class ProcessedPackageViewHolder(private val binding: ItemEmailProcessedPackageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(processedPackage: ProcessedPackageMetadata) {
            binding.tvPackageName.text = processedPackage.packageName
            binding.tvIsPhishy.text = if (processedPackage.isPhishy) "Phishy" else "Safe"
            // Set other TextViews based on the processedPackage object

            binding.btnDelete.setOnClickListener {
                onDeleteClicked(processedPackage.fileName)
            }
        }
    }
}
