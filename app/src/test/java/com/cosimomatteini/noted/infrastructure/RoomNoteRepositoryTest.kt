package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomNoteRepositoryTest {
    @Test
    fun observe_skipsInvalidNotes() = runTest {
        val validNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(status = "UNKNOWN"),
                    noteEntity(id = validNoteId),
                    noteEntity(status = "ARCHIVED", archivedAtMillis = null)
                )
            )
        )

        val notes = repository.observe().first().map { it.id }

        assertEquals(listOf(NoteId(validNoteId)), notes)
    }

    @Test
    fun save_persistsActiveNoteEntity() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao)
        val noteId = UUID.randomUUID()

        repository.save(
            ActiveNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.ofEpochMilli(1_000),
                updatedAt = Instant.ofEpochMilli(2_000)
            )
        )

        assertEquals(
            noteEntity(
                id = noteId,
                title = "Groceries",
                description = "Buy coffee",
                createdAtMillis = 1_000,
                updatedAtMillis = 2_000
            ),
            noteDao.notes.single()
        )
    }

    @Test
    fun save_persistsArchivedNoteEntity() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao)
        val noteId = UUID.randomUUID()

        repository.save(
            ArchivedNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.ofEpochMilli(1_000),
                updatedAt = Instant.ofEpochMilli(2_000),
                archivedAt = Instant.ofEpochMilli(3_000)
            )
        )

        assertEquals(
            noteEntity(
                id = noteId,
                title = "Groceries",
                description = "Buy coffee",
                status = "ARCHIVED",
                archivedAtMillis = 3_000,
                createdAtMillis = 1_000,
                updatedAtMillis = 2_000
            ),
            noteDao.notes.single()
        )
    }

    @Test
    fun delete_removesNoteEntity() = runTest {
        val noteId = UUID.randomUUID()
        val noteDao = InMemoryNoteDao(
            mutableListOf(
                noteEntity(id = noteId)
            )
        )
        val repository = RoomNoteRepository(noteDao)

        repository.delete(NoteId(noteId))

        assertEquals(emptyList<NoteEntity>(), noteDao.notes)
    }

    private class InMemoryNoteDao(val notes: MutableList<NoteEntity> = mutableListOf()) : NoteDao {
        override fun observe(): Flow<List<NoteEntity>> = flowOf(notes)

        override suspend fun load(id: UUID): NoteEntity? = notes.firstOrNull { it.id == id }

        override suspend fun upsert(note: NoteEntity) {
            notes.removeAll { it.id == note.id }
            notes += note
        }

        override suspend fun delete(id: UUID) {
            notes.removeAll { it.id == id }
        }
    }

    private fun noteEntity(
        id: UUID = UUID.randomUUID(),
        title: String = "",
        description: String = "Buy coffee",
        status: String = "ACTIVE",
        archivedAtMillis: Long? = null,
        createdAtMillis: Long = 0,
        updatedAtMillis: Long = 0
    ): NoteEntity = NoteEntity(
        id = id,
        title = title,
        description = description,
        reminderAtMillis = null,
        status = status,
        archivedAtMillis = archivedAtMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}
