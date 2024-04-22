package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.full.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.R
import com.martinszuc.phishing_emails_detection.data.data_repository.local.entity.email_full.Part
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
            binding.headersRecyclerView.layoutManager = LinearLayoutManager(parent.context)
            val headersDivider = DividerItemDecoration(parent.context, LinearLayoutManager.VERTICAL)
            headersDivider.setDrawable(ContextCompat.getDrawable(parent.context, R.drawable.divider_1)!!)
            binding.headersRecyclerView.addItemDecoration(headersDivider)

            binding.bodiesRecyclerView.adapter = bodyAdapter
            binding.bodiesRecyclerView.layoutManager = LinearLayoutManager(parent.context)
            val bodiesDivider = DividerItemDecoration(parent.context, LinearLayoutManager.VERTICAL)
            bodiesDivider.setDrawable(ContextCompat.getDrawable(parent.context, R.drawable.divider_1)!!)
            binding.bodiesRecyclerView.addItemDecoration(bodiesDivider)
        }
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        val part = parts[position]
        holder.binding.partIdValue.text = part.partId
        holder.binding.mimeTypeValue.text = part.mimeType
        holder.binding.filenameValue.text = part.filename

        holder.headerAdapter.headers = part.headers
        holder.headerAdapter.notifyDataSetChanged()

        holder.bodyAdapter.bodies = listOf(part.body)
        holder.bodyAdapter.notifyDataSetChanged()
    }

    override fun getItemCount() = parts.size
}
