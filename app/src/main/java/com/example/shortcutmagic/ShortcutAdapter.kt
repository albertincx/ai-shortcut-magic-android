package com.example.shortcutmagic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shortcutmagic.databinding.ItemShortcutBinding

class ShortcutAdapter(private var items: List<ShortcutEntry>) : RecyclerView.Adapter<ShortcutAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemShortcutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.txtName.text = item.name
        holder.binding.txtData.text = "${item.type}: ${item.data}"
        
        val iconRes = when (item.type) {
            "URL" -> android.R.drawable.ic_menu_directions
            "FILE" -> android.R.drawable.ic_menu_save
            "APP" -> android.R.drawable.ic_menu_view
            else -> android.R.drawable.ic_menu_help
        }
        holder.binding.imgIcon.setImageResource(iconRes)
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ShortcutEntry>) {
        items = newItems
        notifyDataSetChanged()
    }
}