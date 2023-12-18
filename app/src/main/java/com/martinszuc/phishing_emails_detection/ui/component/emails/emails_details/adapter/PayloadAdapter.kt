package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Payload
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsPayloadBinding

/**
 * Authored by matoszuc@gmail.com
 */
class PayloadAdapter : RecyclerView.Adapter<PayloadAdapter.PayloadViewHolder>() {
    var payloads: List<Payload> = listOf()

    inner class PayloadViewHolder(val binding: ItemEmailDetailsPayloadBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val partAdapter = PartAdapter()
        val headerAdapter = HeaderAdapter()
        val bodyAdapter = BodyAdapter()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayloadViewHolder {
        val binding = ItemEmailDetailsPayloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PayloadViewHolder(binding).apply {
            binding.partsRecyclerView.adapter = partAdapter
            binding.partsRecyclerView.layoutManager = LinearLayoutManager(parent.context)
            val partsDivider = DividerItemDecoration(parent.context, LinearLayoutManager.VERTICAL)
            binding.partsRecyclerView.addItemDecoration(partsDivider)

            binding.headersRecyclerView.adapter = headerAdapter
            binding.headersRecyclerView.layoutManager = LinearLayoutManager(parent.context)
            val headersDivider = DividerItemDecoration(parent.context, LinearLayoutManager.VERTICAL)
            binding.headersRecyclerView.addItemDecoration(headersDivider)

            binding.bodiesRecyclerView.adapter = bodyAdapter
            binding.bodiesRecyclerView.layoutManager = LinearLayoutManager(parent.context)
            val bodiesDivider = DividerItemDecoration(parent.context, LinearLayoutManager.VERTICAL)
            binding.bodiesRecyclerView.addItemDecoration(bodiesDivider)
        }
    }


    override fun onBindViewHolder(holder: PayloadViewHolder, position: Int) {
        val payload = payloads[position]
        holder.binding.partId.text = "Part ID: ${payload.partId}"
        holder.binding.mimeType.text = "Mime Type: ${payload.mimeType}"
        holder.binding.filename.text = "Filename: ${payload.filename}"

        holder.partAdapter.parts = payload.parts ?: listOf()  // Use an empty list if parts is null
        holder.partAdapter.notifyDataSetChanged()

        holder.headerAdapter.headers = payload.headers
        holder.headerAdapter.notifyDataSetChanged()

        holder.bodyAdapter.bodies = listOf(payload.body)
        holder.bodyAdapter.notifyDataSetChanged()
    }


    override fun getItemCount() = payloads.size
}
