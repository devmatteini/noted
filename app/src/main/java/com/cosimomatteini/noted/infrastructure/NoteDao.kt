package com.cosimomatteini.noted.infrastructure

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    fun observe(): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsert(note: NoteEntity)
}
