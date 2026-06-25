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
import org.junit.Test

class ImportNotesTest {
    private val now = Instant.parse("2026-06-25T10:00:00Z")

    @Test
    fun importNotes_appendsImportedNotes() = runTest {
        val existingNote = activeNote(title = "Existing")
        val importedNote = activeNote(title = "Imported")
        val repository = InMemoryNoteRepository(existingNote)
        val importNotes = importNotes(repository)

        val result = importNotes(listOf(importedNote)).getOrThrow()

        assertEquals(ImportedNotes(count = 1), result)
        assertEquals(listOf(existingNote, importedNote), repository.notes)
    }

    @Test
    fun importNotes_overridesExistingNotesWithSameUuid() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val oldNote = activeNote(id = noteId, title = "Old")
        val importedNote = activeNote(id = noteId, title = "New")
        val repository = InMemoryNoteRepository(oldNote)

        importNotes(repository)(listOf(importedNote)).getOrThrow()

        assertEquals(listOf(importedNote), repository.notes)
    }

    @Test
    fun importNotes_cancelsAlarmsForActiveImportedNotesOnly() = runTest {
        val activeNote = activeNote()
        val archivedNote = archivedNote()
        val discardedNote = discardedNote()
        val reminderScheduler = InMemoryReminderScheduler()

        importNotes(reminderScheduler = reminderScheduler)(
            listOf(activeNote, archivedNote, discardedNote)
        ).getOrThrow()

        assertEquals(
            listOf(activeNote.id),
            reminderScheduler.cancelled
        )
    }

    @Test
    fun importNotes_schedulesActiveFutureReminders() = runTest {
        val reminderAt = ReminderAt(Instant.parse("2026-06-25T11:00:00Z"))
        val note = activeNote(reminderAt = reminderAt)
        val reminderScheduler = InMemoryReminderScheduler()

        importNotes(reminderScheduler = reminderScheduler)(listOf(note)).getOrThrow()

        assertEquals(listOf(note.id to reminderAt), reminderScheduler.scheduled)
    }

    @Test
    fun importNotes_skipsPastArchivedAndDiscardedReminders() = runTest {
        val pastActiveNote = activeNote(
            reminderAt = ReminderAt(Instant.parse("2026-06-25T09:00:00Z"))
        )
        val archivedNote = archivedNote()
        val discardedNote = discardedNote()
        val reminderScheduler = InMemoryReminderScheduler()

        importNotes(reminderScheduler = reminderScheduler)(
            listOf(pastActiveNote, archivedNote, discardedNote)
        ).getOrThrow()

        assertEquals(emptyList<Pair<NoteId, ReminderAt>>(), reminderScheduler.scheduled)
    }

    private fun importNotes(
        repository: InMemoryNoteRepository = InMemoryNoteRepository(),
        reminderScheduler: InMemoryReminderScheduler = InMemoryReminderScheduler()
    ): ImportNotes = ImportNotes(repository, reminderScheduler, FixedClock(now))

    private fun activeNote(
        id: NoteId = NoteId(UUID.randomUUID()),
        title: String = "Active",
        reminderAt: ReminderAt? = null
    ): ActiveNote = ActiveNote(
        id = id,
        title = NoteTitle.of(title),
        description = NoteDescription.of("Description"),
        reminderAt = reminderAt,
        createdAt = Instant.parse("2026-06-24T10:00:00Z"),
        updatedAt = Instant.parse("2026-06-24T11:00:00Z")
    )

    private fun archivedNote(): ArchivedNote = ArchivedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Archived"),
        description = NoteDescription.of("Description"),
        createdAt = Instant.parse("2026-06-24T10:00:00Z"),
        updatedAt = Instant.parse("2026-06-24T11:00:00Z"),
        archivedAt = Instant.parse("2026-06-24T12:00:00Z")
    )

    private fun discardedNote(): DiscardedNote = DiscardedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Discarded"),
        description = NoteDescription.of("Description"),
        createdAt = Instant.parse("2026-06-24T10:00:00Z"),
        updatedAt = Instant.parse("2026-06-24T11:00:00Z"),
        discardedAt = Instant.parse("2026-06-24T12:00:00Z")
    )
}
