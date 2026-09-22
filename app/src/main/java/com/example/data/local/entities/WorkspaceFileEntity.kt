package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workspace_files")
data class WorkspaceFileEntity(
    @PrimaryKey val path: String, // e.g. "src/MainActivity.kt"
    val name: String,
    val content: String,
    val language: String, // "kotlin", "python", "json", "markdown", "sql"
    val updatedAt: Long = System.currentTimeMillis(),
    val isDirectory: Boolean = false,
    val gitStatus: String = "unmodified" // "modified", "new", "unmodified"
)
