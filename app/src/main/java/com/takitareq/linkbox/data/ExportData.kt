package com.takitareq.linkbox.data

data class ExportData(
    val exportDate: Long = System.currentTimeMillis(),
    val version: String = "1.0",
    val categories: List<CategoryExport>,
    val links: List<LinkExport>
)

data class CategoryExport(
    val id: Long,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class LinkExport(
    val id: Long,
    val categoryId: Long,
    val categoryName: String,
    val title: String,
    val url: String,
    val notes: String,
    val createdAt: Long
)