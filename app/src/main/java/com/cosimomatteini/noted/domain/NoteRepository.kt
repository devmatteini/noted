package com.cosimomatteini.noted.domain

import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observe(): Flow<List<Note>>

    suspend fun loadAll(): List<Note>

    suspend fun load(id: NoteId): Note?

    suspend fun loadActive(id: NoteId): ActiveNote?

    suspend fun loadArchived(id: NoteId): ArchivedNote?

    suspend fun loadDiscarded(id: NoteId): DiscardedNote?

    suspend fun save(note: Note)

    suspend fun delete(id: NoteId)

    suspend fun deleteDiscardedBefore(cutoff: Instant)
}
