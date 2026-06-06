package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.support.FixedClock
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SetNoteReminderTest {
    @Test
    fun setNoteReminder_savesReminder() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val createdAt = Instant.parse("2026-06-06T10:00:00Z")
        val updatedAt = Instant.parse("2026-06-06T11:00:00Z")
        val reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z"))
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )
        val setNoteReminder = SetNoteReminder(repository, FixedClock(updatedAt))

        val result = setNoteReminder(noteId, reminderAt)

        assertTrue(result.isSuccess)
        assertEquals(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = reminderAt,
                createdAt = createdAt,
                updatedAt = updatedAt
            ),
            repository.notes.single()
        )
    }
}
