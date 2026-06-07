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

class ArchiveNoteTest {
    @Test
    fun archiveNote_savesArchivedNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val archivedAt = Instant.parse("2026-06-06T11:00:00Z")
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
        val archiveNote = ArchiveNote(repository, reminderScheduler, FixedClock(archivedAt))

        val result = archiveNote(noteId)

        assertTrue(result.isSuccess)
        assertEquals(
            ArchivedNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = archivedAt,
                archivedAt = archivedAt
            ),
            repository.notes.single()
        )
    }

    @Test
    fun archiveNote_cancelsReminder() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val now = Instant.parse("2026-06-06T10:00:00Z")
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z")),
                createdAt = now,
                updatedAt = now
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val archiveNote = ArchiveNote(repository, reminderScheduler, FixedClock(now))

        archiveNote(noteId)

        assertEquals(listOf(noteId), reminderScheduler.cancelled)
    }
}
