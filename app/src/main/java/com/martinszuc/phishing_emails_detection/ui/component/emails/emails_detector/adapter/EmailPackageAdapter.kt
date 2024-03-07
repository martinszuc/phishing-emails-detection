package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_detector.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackage
import com.martinszuc.phishing_emails_detection.data.email_package.entity.EmailPackageMetadata
import com.martinszuc.phishing_emails_detection.utils.StringUtils

/**
 * Authored by matoszuc@gmail.com
 */

class EmailPackageAdapter(
    private var items: List<EmailPackageMetadata>,
    private val onDeleteClicked: (String) -> Unit
) : RecyclerView.Adapter<EmailPackageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPackageName: TextView = view.findViewById(R.id.tvPackageName)
        val tvIsPhishy: TextView = view.findViewById(R.id.tvIsPhishy)
        val tvCreationDate: TextView = view.findViewById(R.id.tvCreationDate)
        val tvPackageSize: TextView = view.findViewById(R.id.tvPackageSize) // New TextView
        val tvNumberOfEmails: TextView = view.findViewById(R.id.tvNumberOfEmails) // New TextView
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_package_email, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPackageName.text = item.packageName
        holder.tvIsPhishy.text = if (item.isPhishy) "Phishing" else "Safe"
        holder.tvCreationDate.text = StringUtils.formatTimestamp(item.creationDate)
        holder.tvPackageSize.text = "Size: ${item.fileSize} bytes" // Convert bytes to MB
        holder.tvNumberOfEmails.text = "Emails: ${item.numberOfEmails}"
        holder.btnDelete.setOnClickListener {
            onDeleteClicked(items[position].fileName) // Pass the file name to be deleted
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<EmailPackageMetadata>) {
        Log.d("EmailPackageAdapter", "Setting items: ${newItems.size}")
        items = newItems
        notifyDataSetChanged()
    }
}
