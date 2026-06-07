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
import org.junit.Test

class RestoreRemindersTest {
    @Test
    fun restoreReminders_schedulesOnlyActiveFutureReminders() = runTest {
        val now = Instant.parse("2026-06-07T10:00:00Z")
        val futureReminderAt = ReminderAt(Instant.parse("2026-06-07T11:00:00Z"))
        val futureNoteId = NoteId(UUID.randomUUID())
        val pastNoteId = NoteId(UUID.randomUUID())
        val archivedNoteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = futureNoteId,
                title = NoteTitle.of("Future"),
                description = NoteDescription.of("Schedule me"),
                reminderAt = futureReminderAt,
                createdAt = now,
                updatedAt = now
            ),
            ActiveNote(
                id = pastNoteId,
                title = NoteTitle.of("Past"),
                description = NoteDescription.of("Skip me"),
                reminderAt = ReminderAt(Instant.parse("2026-06-07T09:00:00Z")),
                createdAt = now,
                updatedAt = now
            ),
            ArchivedNote(
                id = archivedNoteId,
                title = NoteTitle.of("Archived"),
                description = NoteDescription.of("Skip me too"),
                createdAt = now,
                updatedAt = now,
                archivedAt = now
            )
        )
        val reminderScheduler = InMemoryReminderScheduler()
        val restoreReminders = RestoreReminders(
            repository,
            reminderScheduler,
            FixedClock(now)
        )

        restoreReminders()

        assertEquals(
            listOf(futureNoteId to futureReminderAt),
            reminderScheduler.scheduled
        )
    }
}
