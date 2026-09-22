package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.SkillRegistryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillRegistryDao {
    @Query("SELECT * FROM skill_registry ORDER BY lastUsedTimestamp DESC")
    fun getAllRegisteredSkills(): Flow<List<SkillRegistryEntity>>

    @Query("SELECT * FROM skill_registry WHERE isChainable = 1 ORDER BY executionCount DESC")
    fun getChainableSkills(): Flow<List<SkillRegistryEntity>>

    @Query("SELECT * FROM skill_registry WHERE id = :id")
    suspend fun getSkillById(id: String): SkillRegistryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillRegistryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillRegistryEntity>)

    @Update
    suspend fun updateSkill(skill: SkillRegistryEntity)

    @Query("DELETE FROM skill_registry WHERE id = :id")
    suspend fun deleteSkill(id: String)

    @Query("UPDATE skill_registry SET executionCount = executionCount + 1, lastUsedTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementExecution(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM skill_registry")
    suspend fun clearAll()
}
