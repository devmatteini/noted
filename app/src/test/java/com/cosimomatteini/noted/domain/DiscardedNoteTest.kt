package com.cosimomatteini.noted.domain

import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscardedNoteTest {
    @Test
    fun restore_returnsActiveNoteWithoutReminder() {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val discardedAt = Instant.parse("2026-06-06T11:00:00Z")
        val restoredAt = Instant.parse("2026-06-06T12:00:00Z")
        val note = DiscardedNote(
            id = noteId,
            title = NoteTitle.of("Groceries"),
            description = NoteDescription.of("Buy coffee"),
            createdAt = createdAt,
            updatedAt = discardedAt,
            discardedAt = discardedAt
        )

        val restoredNote = note.restore(restoredAt)

        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = null,
                createdAt = createdAt,
                updatedAt = restoredAt
            ),
            restoredNote
        )
        assertNull(restoredNote.reminderAt)
        assertEquals(false, restoredNote.isPinned)
    }
}
