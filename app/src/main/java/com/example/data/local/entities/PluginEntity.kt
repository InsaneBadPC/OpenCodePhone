package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val installedVersion: String? = null,
    val latestVersion: String,
    val description: String,
    val detailedDescription: String = "",
    val category: String = "Nástroje", // "Nástroje", "Jazyky", "AI & Asistenti", "Databáze", "Verzování", "Vzhled"
    val author: String = "OpenCode Komunita",
    val iconName: String = "extension",
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = true,
    val downloadsCount: Int = 1200,
    val rating: Float = 4.8f,
    val sizeKb: Int = 240,
    val changelog: String = "Opravy chyb a zvýšení stability.",
    val permissions: String = "Čtení souborů, Přístup k síti",
    val websiteUrl: String = "https://opencode.dev/plugins",
    val configJson: String = "{}"
) {
    val hasUpdate: Boolean
        get() = isInstalled && installedVersion != null && installedVersion != latestVersion
}
