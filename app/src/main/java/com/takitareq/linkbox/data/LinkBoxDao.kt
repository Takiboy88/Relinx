package com.takitareq.linkbox.data

import androidx.room.*

@Dao
interface LinkBoxDao {
    @Query("SELECT * FROM categories ORDER BY position ASC, name ASC")
    fun getAllCategories(): List<Category>
    
    @Query("""
        SELECT c.id, c.name, c.color, c.iconRes, c.createdAt, c.position, COUNT(l.id) as linkCount 
        FROM categories c 
        LEFT JOIN links l ON c.id = l.categoryId 
        GROUP BY c.id, c.name, c.color, c.iconRes, c.createdAt, c.position 
        ORDER BY c.position ASC, c.name ASC
    """)
    fun getCategoriesWithLinkCount(): List<CategoryWithLinkCount>
    
    @Query("SELECT * FROM links WHERE categoryId = :categoryId ORDER BY position ASC, createdAt DESC")
    fun getLinksForCategory(categoryId: Long): List<LinkItem>
    
    @Insert
    fun insertCategory(category: Category): Long
    
    @Insert
    fun insertLink(link: LinkItem): Long
    
    @Delete
    fun deleteCategory(category: Category)
    
    @Delete
    fun deleteLink(link: LinkItem)
    
    @Update
    fun updateCategory(category: Category)
    
    @Update
    fun updateLink(link: LinkItem)
    
    // Position update methods for drag & drop
    @Query("UPDATE categories SET position = :newPosition WHERE id = :categoryId")
    suspend fun updateCategoryPosition(categoryId: Long, newPosition: Int)
    
    @Query("UPDATE links SET position = :newPosition WHERE id = :linkId")
    suspend fun updateLinkPosition(linkId: Long, newPosition: Int)
    
    // Get next position for new items
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM categories")
    suspend fun getNextCategoryPosition(): Int
    
    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM links WHERE categoryId = :categoryId")
    suspend fun getNextLinkPosition(categoryId: Long): Int
    
    // Search queries
    @Query("""
        SELECT * FROM links 
        WHERE title LIKE '%' || :searchQuery || '%' 
        OR url LIKE '%' || :searchQuery || '%' 
        OR notes LIKE '%' || :searchQuery || '%' 
        ORDER BY position ASC, createdAt DESC
    """)
    fun searchLinks(searchQuery: String): List<LinkItem>
    
    @Query("""
        SELECT l.* FROM links l 
        INNER JOIN categories c ON l.categoryId = c.id 
        WHERE (l.title LIKE '%' || :searchQuery || '%' 
        OR l.url LIKE '%' || :searchQuery || '%' 
        OR l.notes LIKE '%' || :searchQuery || '%' 
        OR c.name LIKE '%' || :searchQuery || '%')
        AND l.categoryId = :categoryId 
        ORDER BY l.position ASC, l.createdAt DESC
    """)
    fun searchLinksInCategory(categoryId: Long, searchQuery: String): List<LinkItem>
}