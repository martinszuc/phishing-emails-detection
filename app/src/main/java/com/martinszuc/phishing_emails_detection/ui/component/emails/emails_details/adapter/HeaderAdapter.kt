package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.email.local.entity.email_full.Header
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsHeaderBinding

/**
 * Authored by matoszuc@gmail.com
 */
class HeaderAdapter : RecyclerView.Adapter<HeaderAdapter.HeaderViewHolder>() {
    var headers: List<Header> = listOf()

    inner class HeaderViewHolder(val binding: ItemEmailDetailsHeaderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemEmailDetailsHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        val header = headers[position]
        holder.binding.nameValue.text = header.name
        holder.binding.valueValue.text = header.value
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT) // Color 2
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.md_theme_dark_surface)) // Color 1
        }

    }

    override fun getItemCount() = headers.size
}
