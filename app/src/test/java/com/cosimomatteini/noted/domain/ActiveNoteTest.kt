package com.cosimomatteini.noted.domain

import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            reminderAt = ReminderAt(Instant.parse("2026-06-06T11:30:00Z")),
            isPinned = true,
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

    @Test
    fun discard_returnsDiscardedNote() {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val discardedAt = Instant.parse("2026-06-06T12:00:00Z")
        val note = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Groceries"),
            description = NoteDescription.of("Buy coffee"),
            reminderAt = ReminderAt(Instant.parse("2026-06-06T11:30:00Z")),
            isPinned = true,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

        val discardedNote = note.discard(discardedAt)

        assertEquals(
            DiscardedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = discardedAt,
                discardedAt = discardedAt
            ),
            discardedNote
        )
    }

    @Test
    fun setReminder_returnsActiveNoteWithReminder() {
        val note = ActiveNote.empty(Instant.parse("2026-06-06T10:00:00Z"))
        val reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z"))
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")

        val updatedNote = note.setReminder(reminderAt, updatedAt)

        assertEquals(reminderAt, updatedNote.reminderAt)
        assertEquals(updatedAt, updatedNote.updatedAt)
    }

    @Test
    fun clearReminder_returnsActiveNoteWithoutReminder() {
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val note = ActiveNote.empty(Instant.parse("2026-06-06T10:00:00Z"))
            .setReminder(ReminderAt(Instant.parse("2026-06-07T10:00:00Z")), updatedAt)

        val updatedNote = note.clearReminder(Instant.parse("2026-06-06T12:00:00Z"))

        assertNull(updatedNote.reminderAt)
        assertEquals(Instant.parse("2026-06-06T12:00:00Z"), updatedNote.updatedAt)
    }

    @Test
    fun pin_returnsPinnedActiveNote() {
        val note = ActiveNote.empty(Instant.parse("2026-06-06T10:00:00Z"))
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")

        val updatedNote = note.pin(updatedAt)

        assertEquals(true, updatedNote.isPinned)
        assertEquals(updatedAt, updatedNote.updatedAt)
    }

    @Test
    fun unpin_returnsUnpinnedActiveNote() {
        val note = ActiveNote.empty(Instant.parse("2026-06-06T10:00:00Z"))
            .pin(Instant.parse("2026-06-06T11:00:00Z"))
        val updatedAt = Instant.parse("2026-06-06T12:00:00Z")

        val updatedNote = note.unpin(updatedAt)

        assertEquals(false, updatedNote.isPinned)
        assertEquals(updatedAt, updatedNote.updatedAt)
    }
}
