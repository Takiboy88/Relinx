package com.takitareq.linkbox.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int = 0xFF2196F3.toInt(), // Blue color
    val iconRes: Int = android.R.drawable.ic_menu_info_details, // Default icon
    val createdAt: Long = System.currentTimeMillis(),
    val position: Int = 0 // For drag & drop ordering
)