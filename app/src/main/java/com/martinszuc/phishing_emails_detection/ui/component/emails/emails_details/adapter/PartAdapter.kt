package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Part
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsPartBinding

/**
 * Authored by matoszuc@gmail.com
 */
class PartAdapter : RecyclerView.Adapter<PartAdapter.PartViewHolder>() {
    var parts: List<Part> = listOf()

    inner class PartViewHolder(val binding: ItemEmailDetailsPartBinding) : RecyclerView.ViewHolder(binding.root) {
        val headerAdapter = HeaderAdapter()
        val bodyAdapter = BodyAdapter()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val binding = ItemEmailDetailsPartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PartViewHolder(binding).apply {
            binding.headersRecyclerView.adapter = headerAdapter
            binding.headersRecyclerView.layoutManager = LinearLayoutManager(parent.context)  // Add this line

            binding.bodiesRecyclerView.adapter = bodyAdapter
            binding.bodiesRecyclerView.layoutManager = LinearLayoutManager(parent.context)  // Add this line
        }
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = parts[position]
        holder.binding.partId.text = "Part ID: ${part.partId}"
        holder.binding.mimeType.text = "Mime Type: ${part.mimeType}"
        holder.binding.filename.text = "Filename: ${part.filename}"

        holder.headerAdapter.headers = part.headers
        holder.headerAdapter.notifyDataSetChanged()

        holder.bodyAdapter.bodies = listOf(part.body)
        holder.bodyAdapter.notifyDataSetChanged()
    }

    override fun getItemCount() = parts.size
}
