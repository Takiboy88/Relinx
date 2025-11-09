package com.takitareq.linkbox.data

data class CategoryWithLinkCount(
    val id: Long,
    val name: String,
    val color: Int,
    val iconRes: Int,
    val createdAt: Long,
    val position: Int,
    val linkCount: Int
) {
    fun toCategory(): Category {
        return Category(id = id, name = name, color = color, iconRes = iconRes, createdAt = createdAt, position = position)
    }
}