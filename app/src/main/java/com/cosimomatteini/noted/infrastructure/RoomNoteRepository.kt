package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
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

    override suspend fun load(id: NoteId): Note? = noteDao.load(id.value)?.toDomain()?.getOrNull()

    override suspend fun loadActive(id: NoteId): ActiveNote? = load(id) as? ActiveNote

    override suspend fun loadArchived(id: NoteId): ArchivedNote? = load(id) as? ArchivedNote

    override suspend fun loadDiscarded(id: NoteId): DiscardedNote? = load(id) as? DiscardedNote

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
            discardedAtMillis = null,
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
            discardedAtMillis = null,
            createdAtMillis = createdAt.toEpochMilli(),
            updatedAtMillis = updatedAt.toEpochMilli()
        )

        is DiscardedNote -> NoteEntity(
            id = id.value,
            title = title.value,
            description = description.value,
            reminderAtMillis = null,
            status = STATUS_DISCARDED,
            archivedAtMillis = null,
            discardedAtMillis = discardedAt.toEpochMilli(),
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

            STATUS_DISCARDED -> Result.success(
                DiscardedNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    discardedAt = Instant.ofEpochMilli(
                        discardedAtMillis ?: return Result.failure(
                            IllegalArgumentException("Discarded note must have discardedAtMillis.")
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
        const val STATUS_DISCARDED = "DISCARDED"
    }
}
