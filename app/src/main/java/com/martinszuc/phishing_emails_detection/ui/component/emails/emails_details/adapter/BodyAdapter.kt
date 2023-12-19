package com.martinszuc.phishing_emails_detection.ui.component.emails.emails_details.adapter

import android.util.Base64
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
        val decodedData = String(Base64.decode(body.data.replace('-', '+').replace('_', '/'), Base64.DEFAULT)) // TODO Maybe the decoded data should be in the database itself
        holder.binding.dataValue.text = decodedData
        holder.binding.sizeValue.text = body.size.toString()
    }

    override fun getItemCount() = bodies.size
}