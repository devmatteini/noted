package com.cosimomatteini.noted.fixture

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.features.BackupJsonCodec
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

fun main(args: Array<String>) {
    val exportedAt = Instant.now()
    val output = Path.of(args.firstOrNull() ?: defaultOutputName(exportedAt))
    val notes = fixtureNotes(exportedAt)
    val json = BackupJsonCodec(prettyPrint = true).encode(notes, exportedAt)

    output.parent?.let(Files::createDirectories)
    Files.writeString(output, json)
    println("Generated ${notes.size} notes at $output")
}

private fun fixtureNotes(now: Instant): List<Note> {
    val createdAt = now.minusSeconds(7.days)
    val updatedAt = now.minusSeconds(2.days)

    return listOf(
        activeNote(1, title = "", description = "", createdAt = createdAt, updatedAt = updatedAt),
        activeNote(
            2,
            title = "Title only",
            description = "",
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            3,
            title = "",
            description = "Description only",
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            4,
            title = "Title and description",
            description = "Both title and description are present.",
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            5,
            title = "This is a very long title that should span multiple lines",
            description = "Long title scenario.",
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            6,
            title = "Long description",
            description = longDescription(),
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            7,
            title = "Pinned note",
            description = "Pinned active note.",
            isPinned = true,
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            8,
            title = "Reminder today",
            description = "Reminder set for today.",
            reminderAt = ReminderAt(now.plusSeconds(1.hours)),
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            9,
            title = "Reminder in the future",
            description = "Future reminder scenario.",
            reminderAt = ReminderAt(now.plusSeconds(10.days)),
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            10,
            title = "Reminder in the past",
            description = "Past reminder scenario.",
            reminderAt = ReminderAt(now.minusSeconds(3.days)),
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        activeNote(
            11,
            title = "URL description",
            description = "Open https://example.com from this note description.",
            createdAt = createdAt,
            updatedAt = updatedAt
        ),
        ArchivedNote(
            id = noteId(12),
            title = NoteTitle.of("Archived note"),
            description = NoteDescription.of("Archived fixture note."),
            createdAt = createdAt,
            updatedAt = now.minusSeconds(1.days),
            archivedAt = now.minusSeconds(1.days)
        ),
        DiscardedNote(
            id = noteId(13),
            title = NoteTitle.of("Discarded note"),
            description = NoteDescription.of("Discarded fixture note."),
            createdAt = createdAt,
            updatedAt = now.minusSeconds(1.days),
            discardedAt = now.minusSeconds(1.days)
        )
    )
}

private fun activeNote(
    index: Int,
    title: String,
    description: String,
    createdAt: Instant,
    updatedAt: Instant,
    reminderAt: ReminderAt? = null,
    isPinned: Boolean = false
): ActiveNote = ActiveNote(
    id = noteId(index),
    title = NoteTitle.of(title),
    description = NoteDescription.of(description),
    reminderAt = reminderAt,
    isPinned = isPinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)

private fun noteId(index: Int): NoteId = NoteId(
    UUID.fromString("00000000-0000-0000-0000-${index.toString().padStart(12, '0')}")
)

private fun longDescription(): String = buildString {
    repeat(5) { _ ->
        append(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        )
    }
}

private fun defaultOutputName(exportedAt: Instant): String {
    val date = LocalDate.ofInstant(exportedAt, ZoneOffset.UTC)
    return "noted-fixture-$date.json"
}

private val Int.hours: Long get() = this * 60L * 60L

private val Int.days: Long get() = this * 24L * 60L * 60L
