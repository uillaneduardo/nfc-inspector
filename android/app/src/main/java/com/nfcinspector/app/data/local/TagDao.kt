package com.nfcinspector.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM scanned_tags ORDER BY timestamp DESC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM scanned_tags WHERE id = :id LIMIT 1")
    suspend fun getTagById(id: Long): TagEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("DELETE FROM scanned_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("DELETE FROM scanned_tags")
    suspend fun deleteAllTags()
}
