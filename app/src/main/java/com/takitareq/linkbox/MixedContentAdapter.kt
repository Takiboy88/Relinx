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
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.takitareq.linkbox.data.LinkItem

class MixedContentAdapter(
    private val onDeleteLink: (LinkItem) -> Unit,
    private val onLinkMoved: (fromPosition: Int, toPosition: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val TYPE_LINK = 0
        private const val TYPE_AD = 1
        private const val AD_FREQUENCY = 3 // Show ad every 3 links
    }
    
    private var links = mutableListOf<LinkItem>()
    private val mixedItems = mutableListOf<Any>()
    
    fun updateLinks(newLinks: List<LinkItem>) {
        links = newLinks.toMutableList()
        createMixedList()
        notifyDataSetChanged()
    }
    
    fun moveItem(fromPosition: Int, toPosition: Int) {
        // Don't allow moving ads or moving to ad positions
        if (mixedItems[fromPosition] is String || mixedItems[toPosition] is String) {
            return
        }
        
        // Find the actual link positions in the original links list
        val fromLinkPosition = getLinkPositionFromMixedPosition(fromPosition)
        val toLinkPosition = getLinkPositionFromMixedPosition(toPosition)
        
        if (fromLinkPosition != -1 && toLinkPosition != -1) {
            // Move in the links list
            val item = links.removeAt(fromLinkPosition)
            links.add(toLinkPosition, item)
            
            // Recreate mixed list and notify
            createMixedList()
            notifyDataSetChanged()
            
            onLinkMoved(fromLinkPosition, toLinkPosition)
        }
    }
    
    private fun getLinkPositionFromMixedPosition(mixedPosition: Int): Int {
        var linkPosition = 0
        for (i in 0 until mixedPosition) {
            if (mixedItems[i] is LinkItem) {
                linkPosition++
            }
        }
        return if (mixedItems[mixedPosition] is LinkItem) linkPosition else -1
    }
    
    private fun createMixedList() {
        mixedItems.clear()
        
        for (i in links.indices) {
            mixedItems.add(links[i])
            
            // Add ad after every AD_FREQUENCY items (but not at the very end)
            if ((i + 1) % AD_FREQUENCY == 0 && i < links.size - 1) {
                mixedItems.add("AD_PLACEHOLDER")
            }
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (mixedItems[position] is String && mixedItems[position] == "AD_PLACEHOLDER") {
            TYPE_AD
        } else {
            TYPE_LINK
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_AD -> {
                val adView = AdView(parent.context)
                adView.setAdSize(AdSize.BANNER)
                adView.adUnitId = "ca-app-pub-5941977662892783/1824910101" // Inline banner ad unit
                
                // Set layout params for proper spacing
                val layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(16, 16, 16, 16)
                adView.layoutParams = layoutParams
                
                AdViewHolder(adView)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_link, parent, false)
                LinkViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> {
                val link = mixedItems[position] as LinkItem
                holder.bind(link)
            }
            is AdViewHolder -> {
                val adRequest = AdRequest.Builder().build()
                holder.adView.loadAd(adRequest)
            }
        }
    }
    
    override fun getItemCount() = mixedItems.size
    
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
            date.text = timeAgo
            
            itemView.setOnClickListener {
                showLinkActionsMenu(itemView, link)
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
    
    inner class AdViewHolder(val adView: AdView) : RecyclerView.ViewHolder(adView)
}