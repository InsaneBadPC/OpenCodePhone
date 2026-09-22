package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skill_registry")
data class SkillRegistryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val category: String, // e.g., "DevOps", "Testing", "Refactoring", "Git", "Security", "Code Quality"
    val executionStepsJson: String, // JSON array of step descriptions
    val requiredTools: String, // comma-separated, e.g. "terminal, git, test_runner"
    val successScore: Float = 1.0f, // 0.0 - 1.0
    val executionCount: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val isChainable: Boolean = true,
    val inputTemplate: String = "", // e.g. "target_file, commit_msg"
    val outputArtifact: String = "", // e.g. "Clean commit & green tests"
    val autoDetected: Boolean = true,
    val triggerPattern: String = "" // e.g. "test_pass && git_status_clean"
)
