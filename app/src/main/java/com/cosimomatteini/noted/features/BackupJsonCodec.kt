package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.BuildConfig
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class BackupJsonCodec(prettyPrint: Boolean = BuildConfig.DEBUG) {
    private val json = Json {
        explicitNulls = true
        ignoreUnknownKeys = true
        this.prettyPrint = prettyPrint
    }

    fun encode(notes: List<Note>, exportedAt: Instant): String = json.encodeToString(
        BackupFileV1(
            schemaVersion = SCHEMA_VERSION,
            exportedAt = exportedAt.toString(),
            notes = notes.map { it.toBackupNote() }
        )
    )

    fun decode(content: String): Result<List<Note>> = runCatching {
        val root = json.parseToJsonElement(content) as? JsonObject
            ?: return Result.failure(BackupError.MalformedBackup("Root must be a JSON object."))
        val version = root["schemaVersion"]?.jsonPrimitive?.intOrNull
            ?: return Result.failure(
                BackupError.MalformedBackup("Missing or invalid schemaVersion.")
            )

        if (version != SCHEMA_VERSION) {
            return Result.failure(BackupError.UnsupportedVersion(version))
        }

        val backup = json.decodeFromJsonElement<BackupFileV1>(root)
        parseInstant(backup.exportedAt, "exportedAt")
        backup
            .notes
            .distinctByLastId()
            .map { it.toDomain().getOrThrow() }
    }.recoverCatching { failure ->
        when (failure) {
            is BackupError -> throw failure
            is SerializationException -> throw BackupError.MalformedBackup(
                failure.message ?: "JSON does not match backup schema."
            )

            is IllegalArgumentException -> throw BackupError.MalformedBackup(
                failure.message ?: "JSON does not match backup schema."
            )

            else -> throw failure
        }
    }

    private fun Note.toBackupNote(): BackupNoteV1 = when (this) {
        is ActiveNote -> BackupNoteV1(
            id = id.value.toString(),
            status = STATUS_ACTIVE,
            title = title.value,
            description = description.value,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            reminderAt = reminderAt?.value?.toString(),
            archivedAt = null,
            discardedAt = null
        )

        is ArchivedNote -> BackupNoteV1(
            id = id.value.toString(),
            status = STATUS_ARCHIVED,
            title = title.value,
            description = description.value,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            reminderAt = null,
            archivedAt = archivedAt.toString(),
            discardedAt = null
        )

        is DiscardedNote -> BackupNoteV1(
            id = id.value.toString(),
            status = STATUS_DISCARDED,
            title = title.value,
            description = description.value,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
            reminderAt = null,
            archivedAt = null,
            discardedAt = discardedAt.toString()
        )
    }

    private fun List<BackupNoteV1>.distinctByLastId(): List<BackupNoteV1> = asReversed()
        .distinctBy { it.id }
        .asReversed()

    private fun BackupNoteV1.toDomain(): Result<Note> = runCatching {
        val noteId = NoteId(parseUuid(id, "note.id"))
        val noteTitle = NoteTitle.parse(title)
        val noteDescription = NoteDescription.parse(description)
        val noteCreatedAt = parseInstant(createdAt, "note.createdAt")
        val noteUpdatedAt = parseInstant(updatedAt, "note.updatedAt")

        when (status) {
            STATUS_ACTIVE -> ActiveNote(
                id = noteId,
                title = noteTitle,
                description = noteDescription,
                reminderAt = reminderAt?.let { ReminderAt(parseInstant(it, "note.reminderAt")) },
                createdAt = noteCreatedAt,
                updatedAt = noteUpdatedAt
            )

            STATUS_ARCHIVED -> {
                val archivedAtValue = archivedAt
                    ?: throw BackupError.InvalidNote(id, "ARCHIVED note missing archivedAt.")
                ArchivedNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = noteCreatedAt,
                    updatedAt = noteUpdatedAt,
                    archivedAt = parseInstant(archivedAtValue, "note.archivedAt")
                )
            }

            STATUS_DISCARDED -> {
                val discardedAtValue = discardedAt
                    ?: throw BackupError.InvalidNote(id, "DISCARDED note missing discardedAt.")
                DiscardedNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = noteCreatedAt,
                    updatedAt = noteUpdatedAt,
                    discardedAt = parseInstant(discardedAtValue, "note.discardedAt")
                )
            }

            else -> throw BackupError.InvalidNote(id, "Unknown status: $status")
        }
    }.recoverCatching { failure ->
        when (failure) {
            is BackupError -> throw failure
            else -> throw BackupError.InvalidNote(
                id,
                failure.message ?: "Note does not match backup schema."
            )
        }
    }

    private fun parseUuid(value: String, field: String): UUID = try {
        UUID.fromString(value)
    } catch (_: IllegalArgumentException) {
        throw BackupError.InvalidNote(value, "Invalid UUID in $field: $value")
    }

    private fun parseInstant(value: String, field: String): Instant = try {
        Instant.parse(value)
    } catch (_: DateTimeParseException) {
        throw BackupError.InvalidNote(null, "Invalid instant in $field: $value")
    }

    private companion object {
        const val SCHEMA_VERSION = 1
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_ARCHIVED = "ARCHIVED"
        const val STATUS_DISCARDED = "DISCARDED"
    }
}

sealed class BackupError(message: String) : IllegalArgumentException(message) {
    data class UnsupportedVersion(val version: Int) :
        BackupError("Unsupported backup version: $version")

    data class MalformedBackup(val reason: String) :
        BackupError("Malformed backup file: $reason")

    data class InvalidNote(val noteId: String?, val reason: String) :
        BackupError(
            buildString {
                append("Invalid backup note")
                if (noteId != null) append(" $noteId")
                append(": ")
                append(reason)
            }
        )
}

@Serializable
private data class BackupFileV1(
    val schemaVersion: Int,
    val exportedAt: String,
    val notes: List<BackupNoteV1>
)

@Serializable
private data class BackupNoteV1(
    val id: String,
    val status: String,
    val title: String,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val reminderAt: String?,
    val archivedAt: String?,
    val discardedAt: String?
)
