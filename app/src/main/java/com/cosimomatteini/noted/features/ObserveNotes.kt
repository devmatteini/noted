package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteRepository
import kotlinx.coroutines.flow.Flow

class ObserveNotes(
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(): Flow<List<Note>> = noteRepository.observeNotes()
}
