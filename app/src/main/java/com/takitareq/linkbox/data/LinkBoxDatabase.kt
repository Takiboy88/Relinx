package com.takitareq.linkbox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Category::class, LinkItem::class],
    version = 3,
    exportSchema = false
)
abstract class LinkBoxDatabase : RoomDatabase() {
    
    abstract fun linkBoxDao(): LinkBoxDao

    companion object {
        @Volatile
        private var INSTANCE: LinkBoxDatabase? = null
        
        // Migration from version 2 to 3 to add position columns
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add position column to categories table
                database.execSQL("ALTER TABLE categories ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                
                // Add position column to links table
                database.execSQL("ALTER TABLE links ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
                
                // Update existing categories with incremental positions
                database.execSQL("""
                    UPDATE categories 
                    SET position = (
                        SELECT COUNT(*) 
                        FROM categories c2 
                        WHERE c2.id < categories.id
                    )
                """.trimIndent())
                
                // Update existing links with incremental positions within their categories
                database.execSQL("""
                    UPDATE links 
                    SET position = (
                        SELECT COUNT(*) 
                        FROM links l2 
                        WHERE l2.categoryId = links.categoryId AND l2.id < links.id
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): LinkBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LinkBoxDatabase::class.java,
                    "linkbox_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
