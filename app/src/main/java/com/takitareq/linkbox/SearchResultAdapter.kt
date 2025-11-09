package com.takitareq.linkbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.takitareq.linkbox.data.LinkItem

class SearchResultAdapter(
    private val onDeleteLink: (LinkItem) -> Unit,
    private val onOpenCategory: (Long) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder>() {
    
    private var links = listOf<LinkItem>()
    private var categories = mapOf<Long, String>() // categoryId to categoryName mapping
    
    fun updateResults(newLinks: List<LinkItem>, categoryMap: Map<Long, String>) {
        links = newLinks
        categories = categoryMap
        notifyDataSetChanged()
    }
    
    fun clearResults() {
        links = emptyList()
        categories = emptyMap()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return SearchViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(links[position])
    }
    
    override fun getItemCount() = links.size
    
    inner class SearchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favicon: ImageView = itemView.findViewById(R.id.imageViewFavicon)
        private val title: TextView = itemView.findViewById(R.id.textViewTitle)
        private val url: TextView = itemView.findViewById(R.id.textViewUrl)
        private val category: TextView = itemView.findViewById(R.id.textViewCategory)
        private val notes: TextView = itemView.findViewById(R.id.textViewNotes)
        private val date: TextView = itemView.findViewById(R.id.textViewDate)
        
        fun bind(link: LinkItem) {
            title.text = link.title
            url.text = link.url
            
            // Load favicon
            FaviconLoader.loadFavicon(itemView.context, link.url, favicon)
            
            // Show category name
            val categoryName = categories[link.categoryId] ?: "Unknown"
            category.text = "📁 $categoryName"
            
            if (link.notes.isNotEmpty() && link.notes != "Added via share from another app") {
                notes.text = link.notes
                notes.visibility = View.VISIBLE
            } else {
                notes.visibility = View.GONE
            }
            
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                link.createdAt,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            date.text = "Added $timeAgo"
            
            itemView.setOnClickListener {
                showLinkActionsMenu(itemView, link)
            }
            
            category.setOnClickListener {
                onOpenCategory(link.categoryId)
            }
        }
        
        private fun showLinkActionsMenu(view: View, link: LinkItem) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.link_actions_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_open_link -> {
                        openLink(view.context, link.url)
                        true
                    }
                    R.id.action_copy_link -> {
                        copyLinkToClipboard(view.context, link.url)
                        true
                    }
                    R.id.action_share_link -> {
                        shareLink(view.context, link.url, link.title)
                        true
                    }
                    R.id.action_delete_link -> {
                        onDeleteLink(link)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
        
        private fun openLink(context: Context, url: String) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
            }
        }
        
        private fun copyLinkToClipboard(context: Context, url: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Link", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        
        private fun shareLink(context: Context, url: String, title: String) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, url)
                putExtra(Intent.EXTRA_SUBJECT, title)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share link"))
        }
    }
}