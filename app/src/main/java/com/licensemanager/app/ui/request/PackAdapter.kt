package com.licensemanager.app.ui.request

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.licensemanager.app.R
import com.licensemanager.app.data.remote.dto.PackItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import java.util.Locale

class PackAdapter(
    private val onPackSelected: (PackItem) -> Unit
) : ListAdapter<PackItem, PackAdapter.PackViewHolder>(PackDiffCallback()) {

    private var selectedSku: String? = null

    fun setSelectedSku(sku: String?) {
        val old = selectedSku
        selectedSku = sku
        if (old != null) {
            currentList.indexOfFirst { it.sku == old }.takeIf { it >= 0 }?.let { notifyItemChanged(it) }
        }
        if (sku != null) {
            currentList.indexOfFirst { it.sku == sku }.takeIf { it >= 0 }?.let { notifyItemChanged(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pack, parent, false)
        return PackViewHolder(view)
    }

    override fun onBindViewHolder(holder: PackViewHolder, position: Int) {
        holder.bind(getItem(position), selectedSku)
    }

    inner class PackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView as MaterialCardView
        private val tvName: MaterialTextView = itemView.findViewById(R.id.tvPackName)
        private val tvPrice: MaterialTextView = itemView.findViewById(R.id.tvPackPrice)
        private val tvDescription: MaterialTextView = itemView.findViewById(R.id.tvPackDescription)
        private val tvSku: MaterialTextView = itemView.findViewById(R.id.tvPackSku)
        private val tvValidity: MaterialTextView = itemView.findViewById(R.id.tvPackValidity)

        fun bind(pack: PackItem, selectedSku: String?) {
            tvName.text = pack.name
            tvPrice.text = String.format(Locale.US, "$%.2f", pack.price)
            tvSku.text = "SKU: ${pack.sku}"

            val months = pack.validityMonths
            tvValidity.text = "$months month${if (months > 1) "s" else ""}"

            if (!pack.description.isNullOrBlank()) {
                tvDescription.text = pack.description
                tvDescription.visibility = View.VISIBLE
            } else {
                tvDescription.visibility = View.GONE
            }

            val isSelected = pack.sku == selectedSku
            card.isChecked = isSelected
            card.strokeWidth = if (isSelected) 2.dpToPx(itemView) else 0
            card.strokeColor = if (isSelected) {
                itemView.context.getColor(com.google.android.material.R.color.design_default_color_primary)
            } else {
                0
            }

            card.setOnClickListener { onPackSelected(pack) }
        }

        private fun Int.dpToPx(view: View): Int {
            return (this * view.resources.displayMetrics.density).toInt()
        }
    }

    class PackDiffCallback : DiffUtil.ItemCallback<PackItem>() {
        override fun areItemsTheSame(old: PackItem, new: PackItem) = old.sku == new.sku
        override fun areContentsTheSame(old: PackItem, new: PackItem) = old == new
    }
}
