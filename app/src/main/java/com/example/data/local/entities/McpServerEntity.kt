package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mcp_servers")
data class McpServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val transport: String, // "STDIO", "SSE", "HTTP"
    val endpointOrCommand: String,
    val toolsJson: String, // JSON array of exposed tools
    val isInstalled: Boolean = true,
    val isConnected: Boolean = true,
    val envVarsJson: String = "{}"
)
