package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.support.FixedClock
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteExpiredDiscardedNotesTest {
    @Test
    fun deleteExpiredDiscardedNotes_deletesDiscardedNotesAtOrOlderThanRetentionDays() = runTest {
        val now = Instant.parse("2026-06-30T10:00:00Z")
        val expiredNote = discardedNote(discardedAt = Instant.parse("2026-06-23T10:00:00Z"))
        val olderExpiredNote = discardedNote(discardedAt = Instant.parse("2026-06-22T10:00:00Z"))
        val repository = InMemoryNoteRepository(expiredNote, olderExpiredNote)
        val deleteExpiredDiscardedNotes = DeleteExpiredDiscardedNotes(
            repository,
            FixedClock(now),
            retentionDays = 7
        )

        deleteExpiredDiscardedNotes()

        assertEquals(emptyList<DiscardedNote>(), repository.notes)
    }

    @Test
    fun deleteExpiredDiscardedNotes_keepsDiscardedNotesNewerThanRetentionDays() = runTest {
        val now = Instant.parse("2026-06-30T10:00:00Z")
        val retainedNote = discardedNote(discardedAt = Instant.parse("2026-06-23T10:00:01Z"))
        val repository = InMemoryNoteRepository(retainedNote)
        val deleteExpiredDiscardedNotes = DeleteExpiredDiscardedNotes(
            repository,
            FixedClock(now),
            retentionDays = 7
        )

        deleteExpiredDiscardedNotes()

        assertEquals(listOf(retainedNote), repository.notes)
    }

    @Test
    fun deleteExpiredDiscardedNotes_keepsActiveAndArchivedNotes() = runTest {
        val now = Instant.parse("2026-06-30T10:00:00Z")
        val activeNote = ActiveNote(
            id = NoteId(UUID.randomUUID()),
            title = NoteTitle.of("Active"),
            description = NoteDescription.of("Keep"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val archivedNote = ArchivedNote(
            id = NoteId(UUID.randomUUID()),
            title = NoteTitle.of("Archived"),
            description = NoteDescription.of("Keep"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            archivedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(activeNote, archivedNote)
        val deleteExpiredDiscardedNotes = DeleteExpiredDiscardedNotes(repository, FixedClock(now))

        deleteExpiredDiscardedNotes()

        assertEquals(listOf(activeNote, archivedNote), repository.notes)
    }

    private fun discardedNote(discardedAt: Instant): DiscardedNote = DiscardedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Discarded"),
        description = NoteDescription.of("Trash"),
        createdAt = Instant.EPOCH,
        updatedAt = discardedAt,
        discardedAt = discardedAt
    )
}
