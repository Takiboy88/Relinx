package com.takitareq.linkbox.utils

object CategoryIcons {
    
    data class CategoryIcon(
        val iconRes: Int,
        val name: String,
        val description: String
    )
    
    // Predefined category icons
    val AVAILABLE_ICONS = listOf(
        CategoryIcon(android.R.drawable.ic_menu_info_details, "General", "General purpose"),
        CategoryIcon(android.R.drawable.ic_menu_agenda, "Learning", "Books and education"),
        CategoryIcon(android.R.drawable.ic_menu_camera, "Videos", "Video content"),
        CategoryIcon(android.R.drawable.ic_menu_today, "News", "News and current events"),
        CategoryIcon(android.R.drawable.ic_menu_add, "Shopping", "Shopping and purchases"),
        CategoryIcon(android.R.drawable.ic_menu_compass, "Travel", "Travel and places"),
        CategoryIcon(android.R.drawable.ic_menu_gallery, "Pictures", "Images and photos"),
        CategoryIcon(android.R.drawable.ic_menu_manage, "Work", "Professional content"),
        CategoryIcon(android.R.drawable.ic_menu_preferences, "Tools", "Utilities and tools"),
        CategoryIcon(android.R.drawable.ic_menu_recent_history, "History", "Historical content"),
        CategoryIcon(android.R.drawable.ic_menu_search, "Research", "Research and references"),
        CategoryIcon(android.R.drawable.ic_menu_help, "Help", "Help and support"),
        CategoryIcon(android.R.drawable.ic_menu_share, "Social", "Social media"),
        CategoryIcon(android.R.drawable.ic_menu_mylocation, "Local", "Local content"),
        CategoryIcon(android.R.drawable.ic_menu_sort_by_size, "Finance", "Financial content"),
        CategoryIcon(android.R.drawable.ic_menu_week, "Schedule", "Calendar and events"),
        CategoryIcon(android.R.drawable.ic_menu_call, "Contact", "Contacts and communication"),
        CategoryIcon(android.R.drawable.ic_menu_view, "Entertainment", "Entertainment content")
    )
    
    // Default categories with specific icons
    val DEFAULT_CATEGORIES = listOf(
        Pair("General", android.R.drawable.ic_menu_info_details),
        Pair("Learning", android.R.drawable.ic_menu_agenda),
        Pair("Videos", android.R.drawable.ic_menu_camera)
    )
    
    fun getIconByName(name: String): Int {
        return AVAILABLE_ICONS.find { it.name.equals(name, ignoreCase = true) }?.iconRes
            ?: android.R.drawable.ic_menu_info_details
    }
}