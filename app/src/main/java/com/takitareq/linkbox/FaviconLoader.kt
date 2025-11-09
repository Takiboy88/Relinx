package com.takitareq.linkbox

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.net.URL

object FaviconLoader {
    
    fun loadFavicon(context: Context, url: String, imageView: ImageView) {
        try {
            val faviconUrl = getFaviconUrl(url)
            
            val requestOptions = RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_link_placeholder)
                .error(R.drawable.ic_link_placeholder)
                .timeout(5000) // 5 second timeout
            
            Glide.with(context)
                .load(faviconUrl)
                .apply(requestOptions)
                .into(imageView)
                
        } catch (e: Exception) {
            // If there's any error, just show the placeholder
            android.util.Log.e("FaviconLoader", "Error loading favicon: ${e.message}")
            try {
                imageView.setImageResource(R.drawable.ic_link_placeholder)
            } catch (ex: Exception) {
                android.util.Log.e("FaviconLoader", "Error setting placeholder: ${ex.message}")
            }
        }
    }
    
    private fun getFaviconUrl(urlString: String): String {
        return try {
            val url = URL(urlString)
            val protocol = url.protocol
            val host = url.host
            "$protocol://$host/favicon.ico"
        } catch (e: Exception) {
            // Fallback to Google's favicon service
            "https://www.google.com/s2/favicons?domain=${extractDomain(urlString)}&sz=32"
        }
    }
    
    private fun extractDomain(url: String): String {
        return try {
            val cleanUrl = if (!url.startsWith("http")) "https://$url" else url
            URL(cleanUrl).host
        } catch (e: Exception) {
            url
        }
    }
}