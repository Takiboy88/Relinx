package com.takitareq.linkbox.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("linkbox_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_DEFAULT_CATEGORIES_CREATED = "default_categories_created"
        private const val KEY_THEME_MODE = "theme_mode"
    }
    
    var defaultCategoriesCreated: Boolean
        get() = prefs.getBoolean(KEY_DEFAULT_CATEGORIES_CREATED, false)
        set(value) = prefs.edit().putBoolean(KEY_DEFAULT_CATEGORIES_CREATED, value).apply()
    
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()
}