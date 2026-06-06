package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.NoteTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class UpdateNoteTest {
    @Test
    fun updateNote_savesEditedContent() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = createdAt,
            ),
        )
        val updateNote = UpdateNote(repository, FixedClock(updatedAt))

        val result = updateNote(
            id = noteId,
            title = "Errands",
            description = "Pick up package",
        )

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Errands"),
                description = NoteDescription.of("Pick up package"),
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            repository.notes.single(),
        )
    }

    @Test
    fun updateNote_savesEmptyContent() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.EPOCH
        val updatedAt = Instant.ofEpochMilli(1)
        val note = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Errands"),
            description = NoteDescription.of("Buy coffee"),
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        val repository = InMemoryNoteRepository(note)
        val updateNote = UpdateNote(repository, FixedClock(updatedAt))

        val result = updateNote(
            id = noteId,
            title = "",
            description = "",
        )

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of(""),
                description = NoteDescription.of(""),
                createdAt = createdAt,
                updatedAt = updatedAt,
            ),
            repository.notes.single(),
        )
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

    private class FixedClock(
        private val now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }
}
