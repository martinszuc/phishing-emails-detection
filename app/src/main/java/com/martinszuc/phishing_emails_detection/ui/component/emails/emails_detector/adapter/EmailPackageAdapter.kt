package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.StringUtils

class EmailPackageAdapter(
    private var items: List<EmailPackageMetadata>,
    private val onDeleteClicked: (String) -> Unit,
    private val onAddClicked: (View) -> Unit
) : RecyclerView.Adapter<EmailPackageAdapter.EmailPackageViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADD = 0
        private const val VIEW_TYPE_PACKAGE = 1
    }

    sealed class EmailPackageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class AddViewHolder(view: View) : EmailPackageViewHolder(view) {
            // Add view can have its own view initializations if necessary
        }

        class PackageViewHolder(view: View) : EmailPackageViewHolder(view) {
            val tvPackageName: TextView = view.findViewById(R.id.tvPackageName)
            val tvIsPhishy: TextView = view.findViewById(R.id.tvIsPhishy)
            val tvCreationDate: TextView = view.findViewById(R.id.tvCreationDate)
            val tvPackageSize: TextView = view.findViewById(R.id.tvPackageSize)
            val tvNumberOfEmails: TextView = view.findViewById(R.id.tvNumberOfEmails)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailPackageViewHolder {
        return when (viewType) {
            VIEW_TYPE_ADD -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_add_package, parent, false)
                EmailPackageViewHolder.AddViewHolder(view)
            }
            VIEW_TYPE_PACKAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package_email, parent, false)
                EmailPackageViewHolder.PackageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: EmailPackageViewHolder, position: Int) {
        when (holder) {
            is EmailPackageViewHolder.AddViewHolder -> {
                holder.itemView.setOnClickListener {
                    onAddClicked(it)
                }
            }
            is EmailPackageViewHolder.PackageViewHolder -> {
                val realPosition = position - 1 // Adjust for the add item at position 0
                val item = items[realPosition]
                holder.tvPackageName.text = item.packageName
                holder.tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
                holder.tvCreationDate.text = StringUtils.formatTimestamp(item.creationDate)
                holder.tvPackageSize.text = "Size: ${item.fileSize} bytes"
                holder.tvNumberOfEmails.text = "Emails: ${item.numberOfEmails}"
                holder.btnDelete.setOnClickListener {
                    onDeleteClicked(item.fileName)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size + 1 // Add 1 for the add item

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_ADD else VIEW_TYPE_PACKAGE
    }

    fun setItems(newItems: List<EmailPackageMetadata>) {
        items = newItems
        notifyDataSetChanged()
    }

}
