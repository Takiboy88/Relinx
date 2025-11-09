package com.takitareq.linkbox

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.takitareq.linkbox.data.Category
import com.takitareq.linkbox.data.CategoryWithLinkCount

class CategoryAdapter(
    private val onCategoryClick: (Category) -> Unit,
    private val onDeleteCategory: (Category) -> Unit,
    private val onCategoryMoved: (fromPosition: Int, toPosition: Int) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    
    private var categoriesWithCount = mutableListOf<CategoryWithLinkCount>()
    
    fun updateCategories(newCategories: List<CategoryWithLinkCount>) {
        categoriesWithCount = newCategories.toMutableList()
        notifyDataSetChanged()
    }
    
    fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = categoriesWithCount.removeAt(fromPosition)
        categoriesWithCount.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
        onCategoryMoved(fromPosition, toPosition)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categoriesWithCount[position])
    }
    
    override fun getItemCount() = categoriesWithCount.size
    
    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryIcon: ImageView = itemView.findViewById(R.id.imageViewCategoryIcon)
        private val categoryName: TextView = itemView.findViewById(R.id.textViewCategoryName)
        private val linkCount: TextView = itemView.findViewById(R.id.textViewLinkCount)
        private val colorIndicator: View = itemView.findViewById(R.id.viewColorIndicator)
        private val menuButton: ImageButton = itemView.findViewById(R.id.buttonCategoryMenu)
        
        fun bind(categoryWithCount: CategoryWithLinkCount) {
            categoryIcon.setImageResource(categoryWithCount.iconRes)
            categoryName.text = categoryWithCount.name
            linkCount.text = "${categoryWithCount.linkCount} ${if (categoryWithCount.linkCount == 1) "link" else "links"}"
            colorIndicator.setBackgroundColor(categoryWithCount.color)
            
            val category = categoryWithCount.toCategory()
            itemView.setOnClickListener {
                onCategoryClick(category)
            }
            
            menuButton.setOnClickListener {
                showPopupMenu(it, category)
            }
        }
        
        private fun showPopupMenu(view: View, category: Category) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.category_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_delete -> {
                        onDeleteCategory(category)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }
}