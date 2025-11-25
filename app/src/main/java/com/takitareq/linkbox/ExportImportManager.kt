package com.takitareq.linkbox

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.takitareq.linkbox.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class ExportImportManager(private val context: Context, private val database: LinkBoxDatabase) {
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    
    suspend fun exportToJson(): File = withContext(Dispatchers.IO) {
        val categories = database.linkBoxDao().getAllCategories()
        val links = database.linkBoxDao().searchLinks("") // Get all links
        
        // Create export data with category names
        val categoryMap = categories.associateBy { it.id }
        val categoryExports = categories.map { 
            CategoryExport(it.id, it.name)
        }
        val linkExports = links.map { link ->
            LinkExport(
                id = link.id,
                categoryId = link.categoryId,
                categoryName = categoryMap[link.categoryId]?.name ?: "Unknown",
                title = link.title,
                url = link.url,
                notes = link.notes,
                createdAt = link.createdAt
            )
        }
        
        val exportData = ExportData(
            categories = categoryExports,
            links = linkExports
        )
        
        val json = gson.toJson(exportData)
        val fileName = "linkbox_backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
        val file = File(context.filesDir, fileName)
        
        file.writeText(json)
        file
    }
    
    suspend fun exportToCsv(): File = withContext(Dispatchers.IO) {
        val categories = database.linkBoxDao().getAllCategories()
        val links = database.linkBoxDao().searchLinks("") // Get all links
        
        val categoryMap = categories.associateBy { it.id }
        val fileName = "linkbox_links_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.filesDir, fileName)
        
        val csvContent = StringBuilder()
        csvContent.append("Category,Title,URL,Notes,Date Added\n")
        
        links.forEach { link ->
            val categoryName = categoryMap[link.categoryId]?.name ?: "Unknown"
            val dateAdded = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(link.createdAt))
            
            csvContent.append("\"$categoryName\",")
            csvContent.append("\"${link.title.replace("\"", "\"\"")}\",")
            csvContent.append("\"${link.url.replace("\"", "\"\"")}\",")
            csvContent.append("\"${link.notes.replace("\"", "\"\"")}\",")
            csvContent.append("\"$dateAdded\"\n")
        }
        
        file.writeText(csvContent.toString())
        file
    }
    
    suspend fun importFromJson(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val importData = gson.fromJson(json, ExportData::class.java)
            
            var categoriesImported = 0
            var linksImported = 0
            var categoriesSkipped = 0
            var linksSkipped = 0
            
            // Import categories first
            val existingCategories = database.linkBoxDao().getAllCategories()
            val existingCategoryNames = existingCategories.map { it.name.lowercase() }
            val categoryIdMap = mutableMapOf<Long, Long>() // Old ID to new ID mapping
            
            importData.categories.forEach { categoryExport ->
                if (categoryExport.name.lowercase() !in existingCategoryNames) {
                    val newCategory = Category(name = categoryExport.name)
                    val newCategoryId = database.linkBoxDao().insertCategory(newCategory)
                    categoryIdMap[categoryExport.id] = newCategoryId
                    categoriesImported++
                } else {
                    // Map to existing category
                    val existingCategory = existingCategories.find { it.name.lowercase() == categoryExport.name.lowercase() }
                    if (existingCategory != null) {
                        categoryIdMap[categoryExport.id] = existingCategory.id
                    }
                    categoriesSkipped++
                }
            }
            
            // Import links
            val existingLinks = database.linkBoxDao().searchLinks("") // Get all existing links
            val existingUrls = existingLinks.map { it.url.lowercase() }
            
            importData.links.forEach { linkExport ->
                val newCategoryId = categoryIdMap[linkExport.categoryId]
                if (newCategoryId != null && linkExport.url.lowercase() !in existingUrls) {
                    val newLink = LinkItem(
                        categoryId = newCategoryId,
                        title = linkExport.title,
                        url = linkExport.url,
                        notes = linkExport.notes,
                        createdAt = linkExport.createdAt
                    )
                    database.linkBoxDao().insertLink(newLink)
                    linksImported++
                } else {
                    linksSkipped++
                }
            }
            
            ImportResult.Success(categoriesImported, linksImported, categoriesSkipped, linksSkipped)
            
        } catch (e: Exception) {
            ImportResult.Error("Failed to import: ${e.message}")
        }
    }
    
    suspend fun importFromCsv(inputStream: InputStream): ImportResult = withContext(Dispatchers.IO) {
        try {
            val lines = inputStream.bufferedReader().use { it.readLines() }
            if (lines.isEmpty()) {
                return@withContext ImportResult.Error("CSV file is empty")
            }
            
            val dataLines = lines.drop(1) // Skip header
            var categoriesCreated = 0
            var linksImported = 0
            var linksSkipped = 0
            
            val existingCategories = database.linkBoxDao().getAllCategories().associateBy { it.name.lowercase() }
            val categoryCache = mutableMapOf<String, Long>()
            
            val existingLinks = database.linkBoxDao().searchLinks("")
            val existingUrls = existingLinks.map { it.url.lowercase() }
            
            dataLines.forEach { line ->
                val parts = parseCsvLine(line)
                if (parts.size >= 3) {
                    val categoryName = parts[0]
                    val title = parts[1]
                    val url = parts[2]
                    val notes = if (parts.size > 3) parts[3] else ""
                    
                    if (url.lowercase() !in existingUrls) {
                        // Get or create category
                        val categoryId = categoryCache[categoryName.lowercase()] ?: run {
                            val existing = existingCategories[categoryName.lowercase()]
                            if (existing != null) {
                                existing.id
                            } else {
                                val newCategory = Category(name = categoryName)
                                val newId = database.linkBoxDao().insertCategory(newCategory)
                                categoriesCreated++
                                newId
                            }
                        }
                        
                        categoryCache[categoryName.lowercase()] = categoryId
                        
                        val newLink = LinkItem(
                            categoryId = categoryId,
                            title = title,
                            url = url,
                            notes = notes,
                            createdAt = System.currentTimeMillis()
                        )
                        database.linkBoxDao().insertLink(newLink)
                        linksImported++
                    } else {
                        linksSkipped++
                    }
                }
            }
            
            ImportResult.Success(categoriesCreated, linksImported, 0, linksSkipped)
            
        } catch (e: Exception) {
            ImportResult.Error("Failed to import CSV: ${e.message}")
        }
    }
    
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < line.length) {
            val char = line[i]
            when {
                char == '"' && (i == 0 || line[i-1] == ',') -> inQuotes = true
                char == '"' && inQuotes && (i == line.length - 1 || line[i+1] == ',') -> {
                    inQuotes = false
                    if (i < line.length - 1) i++ // Skip the comma
                    result.add(current.toString())
                    current.clear()
                }
                char == '"' && inQuotes && i < line.length - 1 && line[i+1] == '"' -> {
                    current.append('"')
                    i++ // Skip next quote
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(char)
            }
            i++
        }
        
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        
        return result
    }
}

sealed class ImportResult {
    data class Success(
        val categoriesImported: Int,
        val linksImported: Int,
        val categoriesSkipped: Int,
        val linksSkipped: Int
    ) : ImportResult()
    
    data class Error(val message: String) : ImportResult()
}