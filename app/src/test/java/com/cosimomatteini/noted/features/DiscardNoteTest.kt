package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.support.FixedClock
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import com.cosimomatteini.noted.support.InMemoryReminderScheduler
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscardNoteTest {
    @Test
    fun discardNote_discardsActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val discardedAt = Instant.parse("2026-06-06T11:00:00Z")
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val discardNote = DiscardNote(repository, reminderScheduler, FixedClock(discardedAt))

        val result = discardNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(
            DiscardedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = discardedAt,
                discardedAt = discardedAt
            ),
            repository.notes.single()
        )
    }

    @Test
    fun discardNote_discardsArchivedNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val archivedAt = Instant.parse("2026-06-06T10:00:00Z")
        val discardedAt = Instant.parse("2026-06-06T11:00:00Z")
        val repository = InMemoryNoteRepository(
            ArchivedNote(
                id = noteId,
                title = NoteTitle.of("Archived"),
                description = NoteDescription.of("Read-only"),
                createdAt = archivedAt,
                updatedAt = archivedAt,
                archivedAt = archivedAt
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val discardNote = DiscardNote(repository, reminderScheduler, FixedClock(discardedAt))

        val result = discardNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(DiscardedNote::class, repository.notes.single()::class)
        assertTrue(reminderScheduler.cancelled.isEmpty())
    }

    @Test
    fun discardNote_isIdempotentForDiscardedNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val discardedNote = DiscardedNote(
            id = noteId,
            title = NoteTitle.of("Discarded"),
            description = NoteDescription.of("Trash"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            discardedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(discardedNote)
        val reminderScheduler = InMemoryReminderScheduler()
        val discardNote = DiscardNote(repository, reminderScheduler, FixedClock(Instant.EPOCH))

        val result = discardNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(discardedNote, result.getOrThrow())
        assertEquals(discardedNote, repository.notes.single())
        assertTrue(reminderScheduler.cancelled.isEmpty())
    }

    @Test
    fun discardNote_cancelsActiveReminder() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z")),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val discardNote = DiscardNote(repository, reminderScheduler, FixedClock(Instant.EPOCH))

        discardNote(noteId)

        assertEquals(listOf(noteId), reminderScheduler.cancelled)
    }
}
