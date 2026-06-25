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
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportNotesTest {
    @Test
    fun exportNotes_exportsAllLifecycleStates() = runTest {
        val now = Instant.parse("2026-06-25T10:00:00Z")
        val notes = listOf(
            ActiveNote(
                id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
                title = NoteTitle.of("Active"),
                description = NoteDescription.of("Open"),
                createdAt = now,
                updatedAt = now
            ),
            ArchivedNote(
                id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
                title = NoteTitle.of("Archived"),
                description = NoteDescription.of("Stored"),
                createdAt = now,
                updatedAt = now,
                archivedAt = now
            ),
            DiscardedNote(
                id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000003")),
                title = NoteTitle.of("Trash"),
                description = NoteDescription.of("Removed"),
                createdAt = now,
                updatedAt = now,
                discardedAt = now
            )
        )
        val exportNotes = ExportNotes(
            InMemoryNoteRepository(*notes.toTypedArray()),
            FixedClock(now)
        )

        val exportedNotes = exportNotes()
        val decoded = BackupJsonCodec().decode(exportedNotes.json).getOrThrow()

        assertEquals(notes, decoded)
        assertEquals("noted-backup-2026-06-25.json", exportedNotes.filename)
        assertTrue(exportedNotes.json.contains("\"schemaVersion\": 1"))
        assertTrue(exportedNotes.json.contains("\"exportedAt\": \"2026-06-25T10:00:00Z\""))
    }

    @Test
    fun backupFileName_usesUtcDate() {
        assertEquals(
            "noted-backup-2026-06-25.json",
            backupFileName(Instant.parse("2026-06-25T23:59:59Z"))
        )
    }
}
