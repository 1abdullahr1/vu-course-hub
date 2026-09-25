package com.vu.lecturehub.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vu.lecturehub.data.model.ResourceItem
import com.vu.lecturehub.databinding.ItemResourceCardBinding

class ResourceAdapter(
    initialItems: List<ResourceItem> = emptyList(),
    private val onItemClick: (ResourceItem) -> Unit
) : RecyclerView.Adapter<ResourceAdapter.ViewHolder>() {

    private val items = initialItems.toMutableList()

    fun updateItems(newItems: List<ResourceItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResourceCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemResourceCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ResourceItem) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description

            if (!item.badgeText.isNullOrEmpty()) {
                binding.tvBadge.text = item.badgeText
                binding.tvBadge.visibility = View.VISIBLE
            } else {
                binding.tvBadge.visibility = View.GONE
            }

            if (item.iconRes != 0) {
                binding.ivIcon.setImageResource(item.iconRes)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
