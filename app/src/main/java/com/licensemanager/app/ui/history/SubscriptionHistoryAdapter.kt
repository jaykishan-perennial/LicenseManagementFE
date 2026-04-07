package com.licensemanager.app.ui.history

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.licensemanager.app.R
import com.licensemanager.app.data.remote.dto.HistoryItem
import com.licensemanager.app.databinding.ItemSubscriptionHistoryBinding

class SubscriptionHistoryAdapter :
    ListAdapter<HistoryItem, SubscriptionHistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSubscriptionHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemSubscriptionHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HistoryItem) {
            val context = binding.root.context

            binding.tvPackName.text = item.packName ?: "Unknown Pack"

            val status = item.status ?: "unknown"
            binding.tvStatus.text = status.replaceFirstChar { it.uppercase() }

            val (textColor, bgColor) = when (status.lowercase()) {
                "active" -> Pair(
                    context.getColor(R.color.status_active),
                    context.getColor(R.color.status_active_bg)
                )
                "requested", "approved" -> Pair(
                    context.getColor(R.color.status_requested),
                    context.getColor(R.color.status_requested_bg)
                )
                else -> Pair(
                    context.getColor(R.color.status_inactive),
                    context.getColor(R.color.status_inactive_bg)
                )
            }

            binding.tvStatus.setTextColor(textColor)
            binding.tvStatus.background = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = 16f
            }

            val dates = buildString {
                item.assignedAt?.let { append("Assigned: ${it.take(10)}") }
                item.expiresAt?.let {
                    if (isNotEmpty()) append("  ·  ")
                    append("Expires: ${it.take(10)}")
                }
            }
            binding.tvDates.text = dates.ifEmpty { "—" }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean =
            oldItem == newItem
    }
}
