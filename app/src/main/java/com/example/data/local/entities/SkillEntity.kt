package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String, // "Android", "AI", "Security", "DevOps", "Database", "Testing"
    val description: String,
    val systemPrompt: String,
    val tags: String, // comma-separated
    val isInstalled: Boolean = true,
    val isEnabled: Boolean = false,
    val author: String = "OpenCode Core"
)
