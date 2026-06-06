package com.cosimomatteini.noted.domain

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observe(): Flow<List<Note>>

    suspend fun load(id: NoteId): ActiveNote?

    suspend fun save(note: ActiveNote)

    suspend fun delete(id: NoteId)
}
