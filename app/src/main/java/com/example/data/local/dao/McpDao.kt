package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.McpServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface McpDao {
    @Query("SELECT * FROM mcp_servers ORDER BY name ASC")
    fun getAllServers(): Flow<List<McpServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: McpServerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<McpServerEntity>)

    @Update
    suspend fun updateServer(server: McpServerEntity)

    @Query("DELETE FROM mcp_servers WHERE id = :id")
    suspend fun deleteServerById(id: String)
}
