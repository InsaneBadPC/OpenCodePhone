package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.PluginEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PluginDao {
    @Query("SELECT * FROM plugins ORDER BY name ASC")
    fun getAllPlugins(): Flow<List<PluginEntity>>

    @Query("SELECT * FROM plugins WHERE isInstalled = 1 ORDER BY name ASC")
    fun getInstalledPlugins(): Flow<List<PluginEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugin(plugin: PluginEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlugins(plugins: List<PluginEntity>)

    @Update
    suspend fun updatePlugin(plugin: PluginEntity)

    @Query("DELETE FROM plugins WHERE id = :id")
    suspend fun deletePluginById(id: String)

    @Query("SELECT * FROM plugins WHERE id = :id LIMIT 1")
    suspend fun getPluginById(id: String): PluginEntity?
}
