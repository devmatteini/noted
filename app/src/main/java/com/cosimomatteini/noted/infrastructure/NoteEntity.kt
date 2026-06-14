package com.cosimomatteini.noted.infrastructure

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: UUID,
    val title: String,
    val description: String,
    val reminderAtMillis: Long?,
    val status: String,
    val archivedAtMillis: Long?,
    val discardedAtMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
