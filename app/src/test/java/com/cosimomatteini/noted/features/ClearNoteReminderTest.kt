package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
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

class ClearNoteReminderTest {
    @Test
    fun clearNoteReminder_removesReminder() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z")),
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val clearNoteReminder = ClearNoteReminder(
            repository,
            reminderScheduler,
            FixedClock(updatedAt)
        )

        val result = clearNoteReminder(noteId)

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = updatedAt
            ),
            repository.notes.single()
        )
        assertEquals(listOf(noteId), reminderScheduler.cancelled)
    }

    @Test
    fun clearNoteReminder_rejectsArchivedNote() = runTest {
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
        val reminderScheduler = InMemoryReminderScheduler()
        val clearNoteReminder = ClearNoteReminder(
            repository,
            reminderScheduler,
            FixedClock(Instant.EPOCH)
        )

        val result = clearNoteReminder(noteId)

        assertTrue(result.isFailure)
        assertEquals(archivedNote, repository.notes.single())
        assertTrue(reminderScheduler.cancelled.isEmpty())
    }
}
