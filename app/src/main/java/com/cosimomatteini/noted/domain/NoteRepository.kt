package com.cosimomatteini.noted.domain

import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun observe(): Flow<List<Note>>

    suspend fun save(note: ActiveNote)
}
