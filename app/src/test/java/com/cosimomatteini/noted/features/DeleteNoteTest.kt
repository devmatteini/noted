package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.NoteTitle
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteNoteTest {
    @Test
    fun deleteNote_deletesNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
            ),
        )
        val deleteNote = DeleteNote(repository)

        deleteNote(noteId)

        assertTrue(repository.notes.isEmpty())
    }

    private class InMemoryNoteRepository(
        note: ActiveNote,
    ) : NoteRepository {
        val notes = mutableListOf(note)

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
}
