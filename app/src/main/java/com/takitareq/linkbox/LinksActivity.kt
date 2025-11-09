package com.takitareq.linkbox

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.takitareq.linkbox.data.LinkBoxDatabase
import com.takitareq.linkbox.data.LinkItem
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*
import android.widget.LinearLayout

class LinksActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MixedContentAdapter
    private lateinit var emptyView: TextView
    private lateinit var database: LinkBoxDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var categoryId: Long = -1
    private var categoryName: String = ""
    
    // AdMob
    private lateinit var adView: AdView
    
    // Search components
    private lateinit var searchEditText: TextInputEditText
    private var allLinks = listOf<LinkItem>()
    private var isSearchActive = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_links)
        
        categoryId = intent.getLongExtra("categoryId", -1)
        categoryName = intent.getStringExtra("categoryName") ?: "Links"
        
        database = LinkBoxDatabase.getDatabase(this)
        
        setupViews()
        setupRecyclerView()
        setupSearch()
        loadLinks()
    }
    
    private fun setupViews() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = categoryName
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        recyclerView = findViewById(R.id.recyclerViewLinks)
        emptyView = findViewById(R.id.textViewEmpty)
        searchEditText = findViewById(R.id.editTextSearch)
        
        val fab: AppCompatButton = findViewById(R.id.fabAddLink)
        fab.setOnClickListener {
            showAddLinkDialog()
        }
        
        // Setup banner ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
    
    private fun setupRecyclerView() {
        adapter = MixedContentAdapter(
            onDeleteLink = { link ->
                showDeleteConfirmation(link)
            },
            onLinkMoved = { fromPosition, toPosition ->
                updateLinkPositions(fromPosition, toPosition)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        // Setup drag & drop
        val dragDropCallback = DragDropCallback { fromPosition, toPosition ->
            adapter.moveItem(fromPosition, toPosition)
        }
        val itemTouchHelper = ItemTouchHelper(dragDropCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
    
    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null
            
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s?.toString()?.trim() ?: ""
                
                if (query.isEmpty()) {
                    isSearchActive = false
                    adapter.updateLinks(allLinks)
                } else {
                    isSearchActive = true
                    searchJob = scope.launch {
                        delay(300) // Debounce search
                        performSearch(query)
                    }
                }
            }
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
    
    private fun performSearch(query: String) {
        scope.launch {
            try {
                val searchResults = withContext(Dispatchers.IO) {
                    database.linkBoxDao().searchLinksInCategory(categoryId, query)
                }
                
                adapter.updateLinks(searchResults)
                
            } catch (e: Exception) {
                Toast.makeText(this@LinksActivity, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadLinks() {
        scope.launch {
            try {
                val links = withContext(Dispatchers.IO) {
                    database.linkBoxDao().getLinksForCategory(categoryId)
                }
                
                allLinks = links
                
                // If search is not active, show all links
                if (!isSearchActive) {
                    adapter.updateLinks(links)
                }
                
                if (links.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@LinksActivity, "Error loading links: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddLinkDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_link, null)
        val urlEditText = dialogView.findViewById<EditText>(R.id.editTextUrl)
        val titleEditText = dialogView.findViewById<EditText>(R.id.editTextTitle)
        val notesEditText = dialogView.findViewById<EditText>(R.id.editTextNotes)
        
        AlertDialog.Builder(this)
            .setTitle("Add Link")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val url = urlEditText.text.toString().trim()
                val title = titleEditText.text.toString().trim()
                val notes = notesEditText.text.toString().trim()
                
                if (url.isNotEmpty()) {
                    addLink(url, title.ifEmpty { url }, notes)
                } else {
                    Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addLink(url: String, title: String, notes: String) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val nextPosition = database.linkBoxDao().getNextLinkPosition(categoryId)
                    database.linkBoxDao().insertLink(
                        LinkItem(
                            url = url,
                            title = title,
                            notes = notes,
                            categoryId = categoryId,
                            position = nextPosition
                        )
                    )
                }
                loadLinks()
                Toast.makeText(this@LinksActivity, "Link added!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@LinksActivity, "Error adding link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmation(link: LinkItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Link")
            .setMessage("Are you sure you want to delete this link?")
            .setPositiveButton("Delete") { _, _ ->
                deleteLink(link)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteLink(link: LinkItem) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.linkBoxDao().deleteLink(link)
                }
                loadLinks()
                Toast.makeText(this@LinksActivity, "Link deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@LinksActivity, "Error deleting link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateLinkPositions(fromPosition: Int, toPosition: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Update positions for all links in this category
                    val links = database.linkBoxDao().getLinksForCategory(categoryId)
                    links.forEachIndexed { index, link ->
                        database.linkBoxDao().updateLinkPosition(link.id, index)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@LinksActivity, "Error updating positions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}