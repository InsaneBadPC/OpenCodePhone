package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entities.*

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        WorkspaceFileEntity::class,
        SkillEntity::class,
        McpServerEntity::class,
        PluginEntity::class,
        SkillRegistryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun skillDao(): SkillDao
    abstract fun mcpDao(): McpDao
    abstract fun pluginDao(): PluginDao
    abstract fun skillRegistryDao(): SkillRegistryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "opencode.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
