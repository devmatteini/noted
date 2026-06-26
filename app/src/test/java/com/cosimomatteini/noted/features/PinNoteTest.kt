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

class PinNoteTest {
    @Test
    fun pinNote_pinsActiveNote() = runTest {
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
        val pinNote = PinNote(repository, FixedClock(updatedAt))

        val result = pinNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                isPinned = true,
                createdAt = createdAt,
                updatedAt = updatedAt
            ),
            repository.notes.single()
        )
    }

    @Test
    fun pinNote_rejectsArchivedNote() = runTest {
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
        val pinNote = PinNote(repository, FixedClock(Instant.EPOCH))

        val result = pinNote(noteId)

        assertTrue(result.isFailure)
        assertEquals(archivedNote, repository.notes.single())
    }
}
