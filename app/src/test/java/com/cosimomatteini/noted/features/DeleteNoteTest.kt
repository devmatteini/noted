package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import com.cosimomatteini.noted.support.InMemoryReminderScheduler
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteNoteTest {
    @Test
    fun deleteNote_deletesNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val deleteNote = DeleteNote(repository, reminderScheduler)

        deleteNote(noteId)

        assertTrue(repository.notes.isEmpty())
    }

    @Test
    fun deleteNote_cancelsReminder() = runTest {
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
        val deleteNote = DeleteNote(repository, reminderScheduler)

        deleteNote(noteId)

        assertEquals(listOf(noteId), reminderScheduler.cancelled)
    }
}
