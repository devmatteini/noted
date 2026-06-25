package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParseNotesBackupFileTest {
    @Test
    fun parseNotesBackupFile_returnsDomainNotes() {
        val note = ActiveNote(
            id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            title = NoteTitle.of("Imported"),
            description = NoteDescription.of("Description"),
            createdAt = Instant.parse("2026-06-25T10:00:00Z"),
            updatedAt = Instant.parse("2026-06-25T11:00:00Z")
        )
        val content = BackupJsonCodec(prettyPrint = false).encode(
            notes = listOf(note),
            exportedAt = Instant.parse("2026-06-25T12:00:00Z")
        )

        val notes = ParseNotesBackupFile()(content).getOrThrow()

        assertEquals(listOf(note), notes)
    }

    @Test
    fun parseNotesBackupFile_returnsBackupErrors() {
        val content = """
            {
              "schemaVersion": 1,
              "exportedAt": "2026-06-25T10:00:00Z",
              "notes": [
                {
                  "id": "00000000-0000-0000-0000-000000000002",
                  "status": "ARCHIVED",
                  "title": "Invalid",
                  "description": "Missing archivedAt",
                  "createdAt": "2026-06-25T10:00:00Z",
                  "updatedAt": "2026-06-25T10:00:00Z",
                  "reminderAt": null,
                  "archivedAt": null,
                  "discardedAt": null
                }
              ]
            }
        """.trimIndent()

        val result = ParseNotesBackupFile()(content)

        assertTrue(result.exceptionOrNull() is BackupError.InvalidNote)
    }
}
