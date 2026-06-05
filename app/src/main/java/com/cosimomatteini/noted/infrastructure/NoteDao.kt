package com.cosimomatteini.noted.infrastructure

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAtMillis DESC")
    fun observeNotes(): Flow<List<NoteEntity>>
}
