package com.example.data.model

enum class MarketItemType(val titleCzech: String) {
    PLUGIN("Plugin"),
    SKILL("Dovednost"),
    MCP("MCP Server")
}

enum class MarketSource(val titleCzech: String) {
    OFFICIAL("Oficiální"),
    COMMUNITY("Komunitní"),
    GITHUB("Z GitHubu")
}

data class MarketplaceItem(
    val id: String,
    val name: String,
    val type: MarketItemType,
    val source: MarketSource,
    val author: String,
    val version: String,
    val description: String,
    val category: String,
    val githubUrl: String,
    val rating: Float = 4.8f,
    val downloadsCount: String = "1.5k",
    val isVerified: Boolean = true,
    val tags: List<String> = emptyList(),
    // Specific payloads for instant installation
    val permissionsJson: String = "[\"Přístup k souborům\"]",
    val toolsJson: String = "[]",
    val changelog: String = "Stabilní vydání s optimalizacemi pro OpenCode.",
    val systemPrompt: String = "",
    val transport: String = "STDIO",
    val endpointOrCommand: String = ""
)
