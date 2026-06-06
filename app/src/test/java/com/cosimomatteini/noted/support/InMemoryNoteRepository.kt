package com.cosimomatteini.noted.support

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryNoteRepository(
    vararg notes: ActiveNote,
) : NoteRepository {
    val notes = notes.toMutableList()

    override fun observe(): Flow<List<Note>> = flowOf(notes)

    override suspend fun load(id: NoteId): ActiveNote? = notes.firstOrNull { it.id == id }

    override suspend fun save(note: ActiveNote) {
        notes.removeAll { it.id == note.id }
        notes += note
    }

    override suspend fun delete(id: NoteId) {
        notes.removeAll { it.id == id }
    }
}
