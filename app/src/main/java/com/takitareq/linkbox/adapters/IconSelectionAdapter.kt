package com.takitareq.linkbox.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.takitareq.linkbox.R
import com.takitareq.linkbox.utils.CategoryIcons

class IconSelectionAdapter(
    private val icons: List<CategoryIcons.CategoryIcon>,
    private val onIconSelected: (CategoryIcons.CategoryIcon) -> Unit
) : RecyclerView.Adapter<IconSelectionAdapter.IconViewHolder>() {

    private var selectedPosition = 0 // Default to first icon

    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageViewIcon)
        val textView: TextView = itemView.findViewById(R.id.textViewIconName)

        fun bind(icon: CategoryIcons.CategoryIcon, position: Int) {
            imageView.setImageResource(icon.iconRes)
            textView.text = icon.name
            
            // Highlight selected icon
            val isSelected = position == selectedPosition
            itemView.alpha = if (isSelected) 1.0f else 0.6f
            itemView.setBackgroundColor(
                if (isSelected) 
                    ContextCompat.getColor(itemView.context, R.color.selected_icon_background) 
                else Color.TRANSPARENT
            )

            itemView.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPosition)
                notifyItemChanged(selectedPosition)
                onIconSelected(icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_icon, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position], position)
    }

    override fun getItemCount() = icons.size
    
    fun getSelectedIcon(): CategoryIcons.CategoryIcon = icons[selectedPosition]
}