package com.takitareq.linkbox

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * ItemTouchHelper.Callback for implementing drag & drop functionality
 */
class DragDropCallback(
    private val onItemMove: (fromPosition: Int, toPosition: Int) -> Unit
) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // Enable drag in vertical direction only
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // Disable swipe
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        
        // Don't allow moving ads in MixedContentAdapter
        if (isAdViewHolder(viewHolder) || isAdViewHolder(target)) {
            return false
        }
        
        onItemMove(fromPosition, toPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // No swipe action implemented
    }

    override fun isLongPressDragEnabled(): Boolean = true

    override fun isItemViewSwipeEnabled(): Boolean = false

    /**
     * Check if the viewHolder is an ad view (for MixedContentAdapter)
     */
    private fun isAdViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
        return viewHolder.javaClass.simpleName.contains("Ad")
    }
    
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                // Make item slightly transparent while dragging
                viewHolder?.itemView?.alpha = 0.8f
                viewHolder?.itemView?.elevation = 8f
            }
        }
    }
    
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        
        // Restore item appearance
        viewHolder.itemView.alpha = 1.0f
        viewHolder.itemView.elevation = 0f
    }
}