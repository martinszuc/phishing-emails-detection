package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Header
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
        holder.binding.name.text = "Name: ${header.name}"
        holder.binding.value.text = "Value: ${header.value}"
    }

    override fun getItemCount() = headers.size
}
