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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UnarchiveNoteTest {
    @Test
    fun unarchiveNote_savesRestoredActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val archivedAt = Instant.parse("2026-06-06T11:00:00Z")
        val restoredAt = Instant.parse("2026-06-06T12:00:00Z")
        val repository = InMemoryNoteRepository(
            ArchivedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = archivedAt,
                archivedAt = archivedAt
            )
        )
        val unarchiveNote = UnarchiveNote(repository, FixedClock(restoredAt))

        val result = unarchiveNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = null,
                createdAt = createdAt,
                updatedAt = restoredAt
            ),
            repository.notes.single()
        )
        assertNull(result.getOrThrow().reminderAt)
    }

    @Test
    fun unarchiveNote_rejectsActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val activeNote = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Active"),
            description = NoteDescription.of("Editable"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(activeNote)
        val unarchiveNote = UnarchiveNote(repository, FixedClock(Instant.ofEpochMilli(1)))

        val result = unarchiveNote(noteId)

        assertTrue(result.isFailure)
        assertEquals(activeNote, repository.notes.single())
    }
}
