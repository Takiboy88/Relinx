package com.takitareq.linkbox

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.takitareq.linkbox.data.LinkItem

class LinkAdapter(
    private val onDeleteLink: (LinkItem) -> Unit
) : RecyclerView.Adapter<LinkAdapter.LinkViewHolder>() {
    
    private var links = listOf<LinkItem>()
    
    fun updateLinks(newLinks: List<LinkItem>) {
        links = newLinks
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_link, parent, false)
        return LinkViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(links[position])
    }
    
    override fun getItemCount() = links.size
    
    inner class LinkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val favicon: ImageView = itemView.findViewById(R.id.imageViewFavicon)
        private val title: TextView = itemView.findViewById(R.id.textViewTitle)
        private val url: TextView = itemView.findViewById(R.id.textViewUrl)
        private val notes: TextView = itemView.findViewById(R.id.textViewNotes)
        private val date: TextView = itemView.findViewById(R.id.textViewDate)
        
        fun bind(link: LinkItem) {
            title.text = link.title
            url.text = link.url
            
            // Load favicon
            FaviconLoader.loadFavicon(itemView.context, link.url, favicon)
            
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
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link.url))
                    itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    // Handle invalid URL
                }
            }
            
            itemView.setOnLongClickListener {
                onDeleteLink(link)
                true
            }
        }
    }
}