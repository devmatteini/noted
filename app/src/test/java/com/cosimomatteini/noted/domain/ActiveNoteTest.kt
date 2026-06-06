package com.cosimomatteini.noted.domain

import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveNoteTest {
    @Test
    fun archive_returnsArchivedNote() {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val archivedAt = Instant.parse("2026-06-06T12:00:00Z")
        val note = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Groceries"),
            description = NoteDescription.of("Buy coffee"),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        val archivedNote = note.archive(archivedAt)

        assertEquals(
            ArchivedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = archivedAt,
                archivedAt = archivedAt
            ),
            archivedNote
        )
    }
}
