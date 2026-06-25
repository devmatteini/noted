package com.cosimomatteini.noted.support

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InMemoryNoteRepository(vararg notes: Note) : NoteRepository {
    val notes = notes.toMutableList()

    override fun observe(): Flow<List<Note>> = flowOf(notes)

    override suspend fun loadAll(): List<Note> = notes

    override suspend fun load(id: NoteId): Note? = notes.firstOrNull {
        it.id == id
    }

    override suspend fun loadActive(id: NoteId): ActiveNote? = load(id) as? ActiveNote

    override suspend fun loadArchived(id: NoteId): ArchivedNote? = load(id) as? ArchivedNote

    override suspend fun loadDiscarded(id: NoteId): DiscardedNote? = load(id) as? DiscardedNote

    override suspend fun save(note: Note) {
        notes.removeAll { it.id == note.id }
        notes += note
    }

    override suspend fun saveAll(notes: List<Note>) {
        notes.forEach { save(it) }
    }

    override suspend fun delete(id: NoteId) {
        notes.removeAll { it.id == id }
    }

    override suspend fun deleteDiscardedBefore(cutoff: Instant) {
        notes.removeAll { note ->
            note is DiscardedNote && note.discardedAt.toEpochMilli() <= cutoff.toEpochMilli()
        }
    }
}
