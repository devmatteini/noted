package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.NoteTitle
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomNoteRepository(
    private val noteDao: NoteDao,
) : NoteRepository {
    override fun observe(): Flow<List<Note>> =
        noteDao.observe().map { entities ->
            entities.mapNotNull { entity -> entity.toDomain().getOrNull() }
        }

    override suspend fun load(id: NoteId): ActiveNote? {
        val note = noteDao.load(id.value)?.toDomain()?.getOrNull()
        return when (note) {
            is ActiveNote -> note
            is ArchivedNote -> null
            null -> null
        }
    }

    override suspend fun save(note: ActiveNote) {
        noteDao.upsert(note.toEntity())
    }

    private fun ActiveNote.toEntity(): NoteEntity = NoteEntity(
        id = id.value,
        title = title?.value,
        description = description.value,
        reminderAtMillis = null,
        status = STATUS_ACTIVE,
        archivedAtMillis = null,
        createdAtMillis = createdAt.toEpochMilli(),
        updatedAtMillis = updatedAt.toEpochMilli(),
    )

    private fun NoteEntity.toDomain(): Result<Note> {
        val noteId = NoteId(id)
        val noteTitle = NoteTitle.parse(title)
        val noteDescription = NoteDescription.parse(description)
            .getOrElse { return Result.failure(it) }
        val createdAt = Instant.ofEpochMilli(createdAtMillis)
        val updatedAt = Instant.ofEpochMilli(updatedAtMillis)

        return when (status) {
            STATUS_ACTIVE -> Result.success(
                ActiveNote(
                    id = noteId,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                ),
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
                            IllegalArgumentException("Archived note must have archivedAtMillis."),
                        ),
                    ),
                ),
            )

            else -> Result.failure(IllegalArgumentException("Unknown note status: $status"))
        }
    }

    private companion object {
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_ARCHIVED = "ARCHIVED"
    }
}
