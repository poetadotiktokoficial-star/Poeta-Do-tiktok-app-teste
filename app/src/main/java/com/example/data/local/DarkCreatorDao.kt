package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DarkCreatorDao {

    // Content operations
    @Query("SELECT * FROM contents ORDER BY dateAdded DESC")
    fun getAllContents(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM contents WHERE id = :id")
    suspend fun getContentById(id: Long): ContentEntity?

    @Query("SELECT * FROM contents WHERE typeName = :typeName ORDER BY dateAdded DESC")
    fun getContentsByType(typeName: String): Flow<List<ContentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(entity: ContentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContents(entities: List<ContentEntity>)

    @Update
    suspend fun updateContent(entity: ContentEntity)

    @Delete
    suspend fun deleteContent(entity: ContentEntity)

    @Query("DELETE FROM contents WHERE id = :id")
    suspend fun deleteContentById(id: Long)

    // Creations operations
    @Query("SELECT * FROM creations ORDER BY createdAt DESC")
    fun getAllCreations(): Flow<List<GeneratedCreationEntity>>

    @Query("SELECT * FROM creations WHERE id = :id")
    suspend fun getCreationById(id: Long): GeneratedCreationEntity?

    @Query("SELECT * FROM creations WHERE typeName = :typeName ORDER BY createdAt DESC")
    fun getCreationsByType(typeName: String): Flow<List<GeneratedCreationEntity>>

    @Query("SELECT * FROM creations WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteCreations(): Flow<List<GeneratedCreationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreation(entity: GeneratedCreationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreations(entities: List<GeneratedCreationEntity>)

    @Update
    suspend fun updateCreation(entity: GeneratedCreationEntity)

    @Query("UPDATE creations SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteCreationById(id: Long)
}
