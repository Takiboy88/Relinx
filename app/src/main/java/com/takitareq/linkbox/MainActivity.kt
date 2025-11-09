package com.takitareq.linkbox

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.google.android.material.textfield.TextInputEditText
import com.takitareq.linkbox.data.Category
import com.takitareq.linkbox.data.CategoryWithLinkCount
import com.takitareq.linkbox.data.LinkBoxDatabase
import com.takitareq.linkbox.data.LinkItem
import com.takitareq.linkbox.utils.PreferencesManager
import com.takitareq.linkbox.utils.CategoryIcons
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.*
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var emptyView: TextView
    private lateinit var database: LinkBoxDatabase
    private lateinit var preferencesManager: PreferencesManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Search components
    private lateinit var searchEditText: TextInputEditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var searchResultsAdapter: SearchResultAdapter
    private var isSearchMode = false
    
    // Export/Import
    private lateinit var exportImportManager: ExportImportManager
    
    // AdMob
    private lateinit var adView: AdView
    
    // Theme management
    private lateinit var sharedPreferences: SharedPreferences
    private var isDarkMode = false
    
    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize theme preferences first
        sharedPreferences = getSharedPreferences("relinx_prefs", MODE_PRIVATE)
        
        // Apply saved theme before setContentView
        val savedNightMode = sharedPreferences.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedNightMode)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = LinkBoxDatabase.getDatabase(this)
        preferencesManager = PreferencesManager(this)
        exportImportManager = ExportImportManager(this, database)
        
        // Create default categories on first launch
        createDefaultCategoriesIfNeeded()
        
        loadThemePreference()
        
        setupViews()
        setupRecyclerView()
        loadCategories()
        
        // Handle shared content from other apps
        handleSharedContent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        android.util.Log.d("Relinx", "onNewIntent called")
        this.intent = intent
        handleSharedContent()
    }

    private fun setupViews() {
        // Setup toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        
        // Remove default title since we have custom layout
        supportActionBar?.setDisplayShowTitleEnabled(false)
        
        recyclerView = findViewById(R.id.recyclerViewCategories)
        emptyView = findViewById(R.id.textViewEmpty)
        
        try {
            searchEditText = findViewById(R.id.editTextSearch)
            searchResultsRecyclerView = findViewById(R.id.recyclerViewSearchResults)
        } catch (e: Exception) {
            android.util.Log.e("Relinx", "Error finding search views: ${e.message}", e)
        }

        val fab: AppCompatButton = findViewById(R.id.fabAddCategory)
        fab.setOnClickListener {
            showAddCategoryDialog()
        }
        
        // Only setup search if views were found successfully
        if (::searchEditText.isInitialized && ::searchResultsRecyclerView.isInitialized) {
            setupSearch()
        }
        
        // Setup banner ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onCategoryClick = { category ->
                val intent = Intent(this, LinksActivity::class.java)
                intent.putExtra("categoryId", category.id)
                intent.putExtra("categoryName", category.name)
                startActivity(intent)
            },
            onDeleteCategory = { category ->
                showDeleteConfirmation(category)
            },
            onCategoryMoved = { fromPosition, toPosition ->
                updateCategoryPositions(fromPosition, toPosition)
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
        // Setup search results adapter
        searchResultsAdapter = SearchResultAdapter(
            onDeleteLink = { link ->
                showDeleteLinkConfirmation(link)
            },
            onOpenCategory = { categoryId ->
                // Find category name and open it
                scope.launch {
                    val categories = withContext(Dispatchers.IO) {
                        database.linkBoxDao().getAllCategories()
                    }
                    val category = categories.find { it.id == categoryId }
                    if (category != null) {
                        val intent = Intent(this@MainActivity, LinksActivity::class.java)
                        intent.putExtra("categoryId", category.id)
                        intent.putExtra("categoryName", category.name)
                        startActivity(intent)
                    }
                }
            }
        )
        
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchResultsRecyclerView.adapter = searchResultsAdapter
        
        // Setup search text watcher
        searchEditText.addTextChangedListener(object : TextWatcher {
            private var searchJob: Job? = null
            
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                val query = s?.toString()?.trim() ?: ""
                
                if (query.isEmpty()) {
                    exitSearchMode()
                } else {
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

    private fun loadCategories() {
        scope.launch {
            try {
                val categoriesWithCount = withContext(Dispatchers.IO) {
                    database.linkBoxDao().getCategoriesWithLinkCount()
                }

                adapter.updateCategories(categoriesWithCount)

                if (categoriesWithCount.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextCategoryName)
        val recyclerViewIcons = dialogView.findViewById<RecyclerView>(R.id.recyclerViewIcons)
        
        // Setup icon grid
        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 4)
        recyclerViewIcons.layoutManager = gridLayoutManager
        
        var selectedIcon = CategoryIcons.AVAILABLE_ICONS[0] // Default to first icon
        // Set initial category name from first icon
        editTextName.setText(selectedIcon.name)
        
        val iconAdapter = com.takitareq.linkbox.adapters.IconSelectionAdapter(
            CategoryIcons.AVAILABLE_ICONS
        ) { icon ->
            selectedIcon = icon
            // Auto-fill category name when icon is selected
            editTextName.setText(icon.name)
            editTextName.setSelection(editTextName.text?.length ?: 0) // Move cursor to end
        }
        recyclerViewIcons.adapter = iconAdapter

        AlertDialog.Builder(this)
            .setTitle("Add Category")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = editTextName.text.toString().trim()
                if (name.isNotEmpty()) {
                    addCategoryWithIcon(name, selectedIcon.iconRes)
                } else {
                    Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addCategory(name: String) {
        // Default to info icon for backward compatibility
        addCategoryWithIcon(name, android.R.drawable.ic_menu_info_details)
    }
    
    private fun addCategoryWithIcon(name: String, iconRes: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val nextPosition = database.linkBoxDao().getNextCategoryPosition()
                    database.linkBoxDao().insertCategory(
                        Category(name = name, iconRes = iconRes, position = nextPosition)
                    )
                }
                loadCategories()
                Toast.makeText(this@MainActivity, "Category added!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDeleteConfirmation(category: Category) {
        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Are you sure you want to delete '${category.name}' and all its links?")
            .setPositiveButton("Delete") { _, _ ->
                deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCategory(category: Category) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    database.linkBoxDao().deleteCategory(category)
                }
                loadCategories()
                Toast.makeText(this@MainActivity, "Category deleted", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error deleting category: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadCategories()
    }

    private fun handleSharedContent() {
        android.util.Log.d("Relinx", "handleSharedContent called")
        android.util.Log.d("Relinx", "Intent action: ${intent?.action}")
        android.util.Log.d("LinkBox", "Intent type: ${intent?.type}")
        
        if (intent?.action == Intent.ACTION_SEND) {
            when (intent.type) {
                "text/plain" -> {
                    val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
                    val sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT) 
                        ?: intent.getStringExtra(Intent.EXTRA_TITLE)
                    
                    android.util.Log.d("LinkBox", "Shared text: $sharedText")
                    android.util.Log.d("LinkBox", "Shared title: $sharedTitle")
                    
                    if (!sharedText.isNullOrEmpty()) {
                        // Extract URL from shared text if it contains one
                        val url = extractUrlFromText(sharedText) ?: sharedText
                        android.util.Log.d("LinkBox", "Extracted URL: $url")
                        showAddLinkDialog(url, sharedTitle)
                    }
                }
            }
        }
    }
    
    private fun extractUrlFromText(text: String): String? {
        val urlPattern = Regex("https?://[^\\s]+")
        return urlPattern.find(text)?.value
    }
    
    private fun showAddLinkDialog(url: String, title: String?) {
        Toast.makeText(this, "Adding link: ${url.take(30)}...", Toast.LENGTH_SHORT).show()
        
        // First, get all categories to let user choose
        scope.launch {
            try {
                // Make sure database is initialized
                val db = LinkBoxDatabase.getDatabase(this@MainActivity)
                val categories = withContext(Dispatchers.IO) {
                    db.linkBoxDao().getAllCategories()
                }
                
                // Debug: Check how many categories we found
                android.util.Log.d("LinkBox", "Found ${categories.size} categories for sharing")
                for (category in categories) {
                    android.util.Log.d("LinkBox", "Category: ${category.name} (ID: ${category.id})")
                }
                
                // Run dialog creation on main thread
                runOnUiThread {
                    if (categories.isEmpty()) {
                        android.util.Log.d("LinkBox", "No categories found - showing create dialog")
                        // No categories exist, ask user to create one first
                        showCreateCategoryForLinkDialog(url, title)
                    } else {
                        android.util.Log.d("LinkBox", "Categories found - showing selection dialog")
                        // Show category selection dialog
                        showCategorySelectionDialog(categories, url, title)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("LinkBox", "Error loading categories for sharing", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Fallback to create category dialog
                    showCreateCategoryForLinkDialog(url, title)
                }
            }
        }
    }
    
    private fun showCreateCategoryForLinkDialog(url: String, title: String?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val editTextName = dialogView.findViewById<TextInputEditText>(R.id.editTextCategoryName)
        val recyclerViewIcons = dialogView.findViewById<RecyclerView>(R.id.recyclerViewIcons)
        
        // Setup icon grid
        val gridLayoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 4)
        recyclerViewIcons.layoutManager = gridLayoutManager
        
        var selectedIcon = CategoryIcons.AVAILABLE_ICONS[0] // Default to first icon
        // Set initial category name from first icon
        editTextName.setText(selectedIcon.name)
        
        val iconAdapter = com.takitareq.linkbox.adapters.IconSelectionAdapter(
            CategoryIcons.AVAILABLE_ICONS
        ) { icon ->
            selectedIcon = icon
            // Auto-fill category name when icon is selected
            editTextName.setText(icon.name)
            editTextName.setSelection(editTextName.text?.length ?: 0) // Move cursor to end
        }
        recyclerViewIcons.adapter = iconAdapter

        AlertDialog.Builder(this)
            .setTitle("Create Category for Link")
            .setMessage("Create a category for:\n${url.take(50)}${if (url.length > 50) "..." else ""}")
            .setView(dialogView)
            .setPositiveButton("Create & Add Link") { _, _ ->
                val categoryName = editTextName.text.toString().trim()
                if (categoryName.isNotEmpty()) {
                    createCategoryAndAddLinkWithIcon(categoryName, selectedIcon.iconRes, url, title)
                } else {
                    Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                Toast.makeText(this, "Link sharing cancelled", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(true)
            .show()
    }
    
    private fun showCategorySelectionDialog(categories: List<Category>, url: String, title: String?) {
        android.util.Log.d("LinkBox", "showCategorySelectionDialog called with ${categories.size} categories")
        
        if (categories.isEmpty()) {
            android.util.Log.d("LinkBox", "Categories list is empty in selection dialog")
            showCreateCategoryForLinkDialog(url, title)
            return
        }
        
        val categoryNames = categories.map { it.name }.toTypedArray()
        android.util.Log.d("LinkBox", "Category names array: [${categoryNames.joinToString(", ")}]")
        android.util.Log.d("LinkBox", "Array size: ${categoryNames.size}")
        
        // Try a simpler approach - create options that include categories
        val allOptions = mutableListOf<String>()
        allOptions.addAll(categoryNames)
        allOptions.add("➕ Create New Category")
        
        val optionsArray = allOptions.toTypedArray()
        android.util.Log.d("LinkBox", "Final options: [${optionsArray.joinToString(", ")}]")
        
        AlertDialog.Builder(this)
            .setTitle("Select Category for Link")
            .setItems(optionsArray) { dialog, which ->
                android.util.Log.d("LinkBox", "Option selected at index: $which")
                if (which < categories.size) {
                    // Selected an existing category
                    val selectedCategory = categories[which]
                    android.util.Log.d("LinkBox", "Selected existing category: ${selectedCategory.name}")
                    dialog.dismiss()
                    addLinkToCategory(selectedCategory, url, title)
                } else {
                    // Selected "Create New Category"
                    android.util.Log.d("LinkBox", "Selected create new category option")
                    dialog.dismiss()
                    showCreateCategoryForLinkDialog(url, title)
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                android.util.Log.d("LinkBox", "Category selection cancelled")
            }
            .setCancelable(true)
            .show()
    }
    
    private fun createCategoryAndAddLink(categoryName: String, url: String, title: String?) {
        // Default to info icon for backward compatibility
        createCategoryAndAddLinkWithIcon(categoryName, android.R.drawable.ic_menu_info_details, url, title)
    }
    
    private fun createCategoryAndAddLinkWithIcon(categoryName: String, iconRes: Int, url: String, title: String?) {
        scope.launch {
            try {
                val category = Category(name = categoryName, iconRes = iconRes)
                val categoryId = withContext(Dispatchers.IO) {
                    database.linkBoxDao().insertCategory(category)
                }
                
                val linkTitle = title ?: extractTitleFromUrl(url)
                val linkItem = LinkItem(
                    categoryId = categoryId,
                    url = url,
                    title = linkTitle,
                    notes = "Added via share from another app",
                    createdAt = System.currentTimeMillis()
                )
                
                withContext(Dispatchers.IO) {
                    database.linkBoxDao().insertLink(linkItem)
                }
                
                loadCategories()
                Toast.makeText(this@MainActivity, "Link added to new category '$categoryName'!", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error adding link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addLinkToCategory(category: Category, url: String, title: String?) {
        scope.launch {
            try {
                val linkTitle = title ?: extractTitleFromUrl(url)
                val linkItem = LinkItem(
                    categoryId = category.id,
                    url = url,
                    title = linkTitle,
                    notes = "Added via share from another app",
                    createdAt = System.currentTimeMillis()
                )
                
                withContext(Dispatchers.IO) {
                    database.linkBoxDao().insertLink(linkItem)
                }
                
                Toast.makeText(this@MainActivity, "Link added to '${category.name}'!", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error adding link: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun extractTitleFromUrl(url: String): String {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.host ?: url
        } catch (e: Exception) {
            url
        }
    }
    
    private fun performSearch(query: String) {
        scope.launch {
            try {
                val searchResults = withContext(Dispatchers.IO) {
                    database.linkBoxDao().searchLinks(query)
                }
                
                val categories = withContext(Dispatchers.IO) {
                    database.linkBoxDao().getAllCategories()
                }
                
                val categoryMap = categories.associate { it.id to it.name }
                
                enterSearchMode()
                searchResultsAdapter.updateResults(searchResults, categoryMap)
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Search error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun enterSearchMode() {
        if (!isSearchMode) {
            isSearchMode = true
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.VISIBLE
        }
    }
    
    private fun exitSearchMode() {
        if (isSearchMode) {
            isSearchMode = false
            searchResultsRecyclerView.visibility = View.GONE
            searchResultsAdapter.clearResults()
            loadCategories() // This will show categories again
        }
    }
    
    private fun showDeleteLinkConfirmation(link: LinkItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Link")
            .setMessage("Are you sure you want to delete '${link.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            database.linkBoxDao().deleteLink(link)
                        }
                        
                        // Refresh search results if in search mode
                        val currentQuery = searchEditText.text?.toString()?.trim() ?: ""
                        if (isSearchMode && currentQuery.isNotEmpty()) {
                            performSearch(currentQuery)
                        }
                        
                        Toast.makeText(this@MainActivity, "Link deleted", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Error deleting link: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_export -> {
                showExportDialog()
                true
            }
            R.id.menu_import -> {
                showImportDialog()
                true
            }
            R.id.menu_theme_toggle -> {
                toggleTheme()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val themeMenuItem = menu?.findItem(R.id.menu_theme_toggle)
        themeMenuItem?.title = if (isDarkMode) "☀️ Light Mode" else "🌙 Dark Mode"
        return super.onPrepareOptionsMenu(menu)
    }
    
    private fun showExportDialog() {
        AlertDialog.Builder(this)
            .setTitle("Export LinkBox Data")
            .setMessage("Choose export format:")
            .setPositiveButton("JSON (Full Backup)") { _, _ ->
                exportData("json")
            }
            .setNegativeButton("CSV (Links Only)") { _, _ ->
                exportData("csv")
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun exportData(format: String) {
        scope.launch {
            try {
                val file = when (format) {
                    "json" -> exportImportManager.exportToJson()
                    "csv" -> exportImportManager.exportToCsv()
                    else -> return@launch
                }
                
                shareFile(file)
                Toast.makeText(this@MainActivity, "Data exported to ${file.name}", Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun shareFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when {
                    file.name.endsWith(".json") -> "application/json"
                    file.name.endsWith(".csv") -> "text/csv"
                    else -> "application/octet-stream"
                }
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "LinkBox Export - ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "LinkBox backup file")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(intent, "Share LinkBox Export"))
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showImportDialog() {
        AlertDialog.Builder(this)
            .setTitle("Import LinkBox Data")
            .setMessage("Select a LinkBox backup file (JSON or CSV) to import.\n\nNote: Duplicate links will be skipped.")
            .setPositiveButton("Choose File") { _, _ ->
                importLauncher.launch("*/*")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun importFile(uri: Uri) {
        scope.launch {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val fileName = uri.lastPathSegment ?: ""
                    val result = when {
                        fileName.endsWith(".json", ignoreCase = true) -> 
                            exportImportManager.importFromJson(inputStream)
                        fileName.endsWith(".csv", ignoreCase = true) -> 
                            exportImportManager.importFromCsv(inputStream)
                        else -> {
                            // Try to detect format by content
                            val content = inputStream.bufferedReader().use { it.readText() }
                            inputStream.close()
                            
                            if (content.trim().startsWith("{")) {
                                // Looks like JSON
                                exportImportManager.importFromJson(content.byteInputStream())
                            } else {
                                // Assume CSV
                                exportImportManager.importFromCsv(content.byteInputStream())
                            }
                        }
                    }
                    
                    when (result) {
                        is ImportResult.Success -> {
                            val message = buildString {
                                append("Import completed!\n")
                                append("Categories imported: ${result.categoriesImported}\n")
                                append("Links imported: ${result.linksImported}\n")
                                if (result.categoriesSkipped > 0) {
                                    append("Categories skipped: ${result.categoriesSkipped}\n")
                                }
                                if (result.linksSkipped > 0) {
                                    append("Links skipped: ${result.linksSkipped}")
                                }
                            }
                            
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Import Success")
                                .setMessage(message)
                                .setPositiveButton("OK") { _, _ ->
                                    loadCategories() // Refresh the UI
                                }
                                .show()
                        }
                        is ImportResult.Error -> {
                            AlertDialog.Builder(this@MainActivity)
                                .setTitle("Import Error")
                                .setMessage(result.message)
                                .setPositiveButton("OK", null)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Could not read file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadThemePreference() {
        val currentNightMode = AppCompatDelegate.getDefaultNightMode()
        isDarkMode = when (currentNightMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> {
                // MODE_NIGHT_FOLLOW_SYSTEM or other - check system
                resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }
    }
    
    private fun toggleTheme() {
        isDarkMode = !isDarkMode
        val newMode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        
        // Save preference first
        sharedPreferences.edit()
            .putInt("night_mode", newMode)
            .apply()
        
        // Apply the theme change
        AppCompatDelegate.setDefaultNightMode(newMode)
        
        // Show toast to confirm the action
        Toast.makeText(this, "Theme changed to ${if (isDarkMode) "Dark" else "Light"} mode", Toast.LENGTH_SHORT).show()
    }
    
    private fun createDefaultCategoriesIfNeeded() {
        if (!preferencesManager.defaultCategoriesCreated) {
            scope.launch(Dispatchers.IO) {
                try {
                    CategoryIcons.DEFAULT_CATEGORIES.forEach { (name, iconRes) ->
                        val category = Category(
                            name = name,
                            iconRes = iconRes,
                            color = when (name) {
                                "General" -> 0xFF2196F3.toInt() // Blue
                                "Learning" -> 0xFF4CAF50.toInt() // Green
                                "Videos" -> 0xFFF44336.toInt() // Red
                                else -> 0xFF2196F3.toInt()
                            }
                        )
                        database.linkBoxDao().insertCategory(category)
                    }
                    preferencesManager.defaultCategoriesCreated = true
                    
                    // Refresh the categories list on the main thread
                    withContext(Dispatchers.Main) {
                        loadCategories()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LinkBox", "Error creating default categories", e)
                }
            }
        }
    }
    
    private fun updateCategoryPositions(fromPosition: Int, toPosition: Int) {
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    // Update positions for all affected categories
                    val categories = database.linkBoxDao().getAllCategories()
                    categories.forEachIndexed { index, category ->
                        database.linkBoxDao().updateCategoryPosition(category.id, index)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error updating positions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}