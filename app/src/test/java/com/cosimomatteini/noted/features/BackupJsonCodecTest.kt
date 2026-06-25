package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupJsonCodecTest {
    private val codec = BackupJsonCodec()

    @Test
    fun encodeDecode_activeNote() {
        val note = ActiveNote(
            id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
            title = NoteTitle.of("Groceries"),
            description = NoteDescription.of("Buy coffee"),
            reminderAt = ReminderAt(Instant.parse("2026-06-07T10:00:00Z")),
            createdAt = Instant.parse("2026-06-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-06-02T10:00:00Z")
        )

        val decoded = codec.decode(codec.encode(listOf(note), exportedAt)).getOrThrow()

        assertEquals(listOf(note), decoded)
    }

    @Test
    fun encodeDecode_archivedNote() {
        val note = ArchivedNote(
            id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
            title = NoteTitle.of("Archive"),
            description = NoteDescription.of("Stored"),
            createdAt = Instant.parse("2026-06-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-06-03T10:00:00Z"),
            archivedAt = Instant.parse("2026-06-03T10:00:00Z")
        )

        val decoded = codec.decode(codec.encode(listOf(note), exportedAt)).getOrThrow()

        assertEquals(listOf(note), decoded)
    }

    @Test
    fun encodeDecode_discardedNote() {
        val note = DiscardedNote(
            id = NoteId(UUID.fromString("00000000-0000-0000-0000-000000000003")),
            title = NoteTitle.of("Trash"),
            description = NoteDescription.of("Removed"),
            createdAt = Instant.parse("2026-06-01T10:00:00Z"),
            updatedAt = Instant.parse("2026-06-04T10:00:00Z"),
            discardedAt = Instant.parse("2026-06-04T10:00:00Z")
        )

        val decoded = codec.decode(codec.encode(listOf(note), exportedAt)).getOrThrow()

        assertEquals(listOf(note), decoded)
    }

    @Test
    fun decode_rejectsUnsupportedVersion() {
        val result = codec.decode(
            """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-06-25T10:00:00Z",
              "notes": []
            }
            """.trimIndent()
        )

        assertEquals(BackupError.UnsupportedVersion(2), result.exceptionOrNull())
    }

    @Test
    fun decode_rejectsMalformedJson() {
        val result = codec.decode("not json")

        assertTrue(result.exceptionOrNull() is BackupError.MalformedBackup)
    }

    @Test
    fun decode_rejectsInvalidLifecycleFields() {
        val result = codec.decode(
            backupJson(
                noteJson = """
                {
                  "id": "00000000-0000-0000-0000-000000000004",
                  "status": "ARCHIVED",
                  "title": "Archive",
                  "description": "Stored",
                  "createdAt": "2026-06-01T10:00:00Z",
                  "updatedAt": "2026-06-03T10:00:00Z",
                  "reminderAt": null,
                  "archivedAt": null,
                  "discardedAt": null
                }
                """.trimIndent()
            )
        )

        val error = result.exceptionOrNull()

        assertTrue(error is BackupError.InvalidNote)
        assertTrue(error!!.message!!.contains("missing archivedAt"))
    }

    @Test
    fun decode_ignoresFieldsNotUsedByStatus() {
        val noteId = "00000000-0000-0000-0000-000000000006"
        val result = codec.decode(
            backupJson(
                noteJson = """
                {
                  "id": "$noteId",
                  "status": "ACTIVE",
                  "title": "Active",
                  "description": "Keep extra fields ignored",
                  "createdAt": "2026-06-01T10:00:00Z",
                  "updatedAt": "2026-06-02T10:00:00Z",
                  "reminderAt": null,
                  "archivedAt": "2026-06-03T10:00:00Z",
                  "discardedAt": "2026-06-04T10:00:00Z"
                }
                """.trimIndent()
            )
        )

        val decoded = result.getOrThrow()

        assertEquals(1, decoded.size)
        assertTrue(decoded.single() is ActiveNote)
    }

    @Test
    fun decode_keepsLastDuplicateId() {
        val noteId = "00000000-0000-0000-0000-000000000005"
        val result = codec.decode(
            backupJson(
                noteJson = """
                {
                  "id": "$noteId",
                  "status": "ACTIVE",
                  "title": "First",
                  "description": "Old",
                  "createdAt": "2026-06-01T10:00:00Z",
                  "updatedAt": "2026-06-02T10:00:00Z",
                  "reminderAt": null,
                  "archivedAt": null,
                  "discardedAt": null
                },
                {
                  "id": "$noteId",
                  "status": "ACTIVE",
                  "title": "Last",
                  "description": "New",
                  "createdAt": "2026-06-01T10:00:00Z",
                  "updatedAt": "2026-06-03T10:00:00Z",
                  "reminderAt": null,
                  "archivedAt": null,
                  "discardedAt": null
                }
                """.trimIndent()
            )
        )

        val decoded = result.getOrThrow()

        assertEquals(1, decoded.size)
        assertTrue(decoded.single() is ActiveNote)
        assertEquals("Last", decoded.single().title.value)
    }

    private fun backupJson(noteJson: String): String = """
        {
          "schemaVersion": 1,
          "exportedAt": "2026-06-25T10:00:00Z",
          "notes": [
            $noteJson
          ]
        }
    """.trimIndent()

    private companion object {
        val exportedAt: Instant = Instant.parse("2026-06-25T10:00:00Z")
    }
}
