package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Logger
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteRepository(private val noteDao: NoteDao, private val logger: Logger) :
    NoteRepository {
    override fun observe(): Flow<List<Note>> = noteDao.observe().map { entities ->
        entities.mapNotNull { entity ->
            entity.toDomain().getOrElse { failure ->
                logger.warn(TAG, "Skipping invalid note ${entity.id}", failure)
                null
            }
        }
    }

    override suspend fun load(id: NoteId): ActiveNote? {
        val note = noteDao.load(id.value)?.toDomain()?.getOrNull()
        return when (note) {
            is ActiveNote -> note
            is ArchivedNote -> null
            null -> null
        }
    }

    override suspend fun save(note: Note) {
        noteDao.upsert(note.toEntity())
    }

    override suspend fun delete(id: NoteId) {
        noteDao.delete(id.value)
    }

    private fun Note.toEntity(): NoteEntity = when (this) {
        is ActiveNote -> NoteEntity(
            id = id.value,
            title = title.value,
            description = description.value,
            reminderAtMillis = reminderAt?.value?.toEpochMilli(),
            status = STATUS_ACTIVE,
            archivedAtMillis = null,
            createdAtMillis = createdAt.toEpochMilli(),
            updatedAtMillis = updatedAt.toEpochMilli()
        )

        is ArchivedNote -> NoteEntity(
            id = id.value,
            title = title.value,
            description = description.value,
            reminderAtMillis = null,
            status = STATUS_ARCHIVED,
            archivedAtMillis = archivedAt.toEpochMilli(),
            createdAtMillis = createdAt.toEpochMilli(),
            updatedAtMillis = updatedAt.toEpochMilli()
        )
    }

    private fun NoteEntity.toDomain(): Result<Note> {
        val noteId = NoteId(id)
        val noteTitle = NoteTitle.parse(title)
        val noteDescription = NoteDescription.parse(description)
        val createdAt = Instant.ofEpochMilli(createdAtMillis)
        val updatedAt = Instant.ofEpochMilli(updatedAtMillis)

        return when (status) {
            STATUS_ACTIVE -> Result.success(
                ActiveNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    reminderAt = reminderAtMillis?.let { ReminderAt(Instant.ofEpochMilli(it)) },
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
            )

            STATUS_ARCHIVED -> Result.success(
                ArchivedNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    archivedAt = Instant.ofEpochMilli(
                        archivedAtMillis ?: return Result.failure(
                            IllegalArgumentException("Archived note must have archivedAtMillis.")
                        )
                    )
                )
            )

            else -> Result.failure(IllegalArgumentException("Unknown note status: $status"))
        }
    }

    private companion object {
        const val TAG = "RoomNoteRepository"
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_ARCHIVED = "ARCHIVED"
    }
}
