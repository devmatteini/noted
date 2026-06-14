package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.support.FixedClock
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
                updatedAt = createdAt
            )
        )
        val updateNote = UpdateNote(repository, FixedClock(updatedAt))

        val result = updateNote(
            id = noteId,
            title = "Errands",
            description = "Pick up package"
        )

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Errands"),
                description = NoteDescription.of("Pick up package"),
                createdAt = createdAt,
                updatedAt = updatedAt
            ),
            repository.notes.single()
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
            updatedAt = createdAt
        )
        val repository = InMemoryNoteRepository(note)
        val updateNote = UpdateNote(repository, FixedClock(updatedAt))

        val result = updateNote(
            id = noteId,
            title = "",
            description = ""
        )

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of(""),
                description = NoteDescription.of(""),
                createdAt = createdAt,
                updatedAt = updatedAt
            ),
            repository.notes.single()
        )
    }

    @Test
    fun updateNote_rejectsArchivedNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val archivedNote = ArchivedNote(
            id = noteId,
            title = NoteTitle.of("Archived"),
            description = NoteDescription.of("Read-only"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            archivedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(archivedNote)
        val updateNote = UpdateNote(repository, FixedClock(Instant.ofEpochMilli(1)))

        val result = updateNote(noteId, "Updated", "Changed")

        assertTrue(result.isFailure)
        assertEquals(archivedNote, repository.notes.single())
    }
}
