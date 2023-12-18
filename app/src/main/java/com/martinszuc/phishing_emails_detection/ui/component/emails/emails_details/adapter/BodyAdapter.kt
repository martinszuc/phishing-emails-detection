package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.martinszuc.phishing_emails_detection.data.local.entity.email_full.Body
import com.martinszuc.phishing_emails_detection.databinding.ItemEmailDetailsBodyBinding

/**
 * Authored by matoszuc@gmail.com
 */
class BodyAdapter : RecyclerView.Adapter<BodyAdapter.BodyViewHolder>() {
    var bodies: List<Body> = listOf()

    inner class BodyViewHolder(val binding: ItemEmailDetailsBodyBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BodyViewHolder {
        val binding = ItemEmailDetailsBodyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BodyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BodyViewHolder, position: Int) {
        val body = bodies[position]
        holder.binding.data.text = "Data: ${body.data}"
        holder.binding.size.text = "Size: ${body.size}"
    }

    override fun getItemCount() = bodies.size
}
