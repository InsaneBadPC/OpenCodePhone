package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.WorkspaceFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspace_files ORDER BY isDirectory DESC, path ASC")
    fun getAllFiles(): Flow<List<WorkspaceFileEntity>>

    @Query("SELECT * FROM workspace_files WHERE path = :path LIMIT 1")
    suspend fun getFileByPath(path: String): WorkspaceFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: WorkspaceFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<WorkspaceFileEntity>)

    @Query("DELETE FROM workspace_files WHERE path = :path")
    suspend fun deleteFileByPath(path: String)

    @Query("DELETE FROM workspace_files")
    suspend fun clearAllFiles()
}
