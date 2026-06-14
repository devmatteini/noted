package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.DiscardedNote
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

class RestoreDiscardedNoteTest {
    @Test
    fun restoreDiscardedNote_savesRestoredActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val discardedAt = Instant.parse("2026-06-06T11:00:00Z")
        val restoredAt = Instant.parse("2026-06-06T12:00:00Z")
        val repository = InMemoryNoteRepository(
            DiscardedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = discardedAt,
                discardedAt = discardedAt
            )
        )
        val restoreNote = RestoreDiscardedNote(repository, FixedClock(restoredAt))

        val result = restoreNote(noteId)

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
    fun restoreDiscardedNote_rejectsActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val activeNote = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Active"),
            description = NoteDescription.of("Editable"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(activeNote)
        val restoreNote = RestoreDiscardedNote(repository, FixedClock(Instant.ofEpochMilli(1)))

        val result = restoreNote(noteId)

        assertTrue(result.isFailure)
        assertEquals(activeNote, repository.notes.single())
    }
}
