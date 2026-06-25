package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.support.EmptyLogger
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
                    noteEntity(status = "ARCHIVED", archivedAtMillis = null),
                    noteEntity(status = "DISCARDED", discardedAtMillis = null)
                )
            ),
            EmptyLogger
        )

        val notes = repository.observe().first().map { it.id }

        assertEquals(listOf(NoteId(validNoteId)), notes)
    }

    @Test
    fun loadAll_returnsValidNotes() = runTest {
        val noteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = noteId),
                    noteEntity(status = "UNKNOWN")
                )
            ),
            EmptyLogger
        )

        val notes = repository.loadAll()

        assertEquals(listOf(NoteId(noteId)), notes.map { it.id })
    }

    @Test
    fun save_persistsActiveNoteEntity() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao, EmptyLogger)
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
    fun save_persistsActiveNoteReminder() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao, EmptyLogger)
        val reminderAt = Instant.ofEpochMilli(3_000)

        repository.save(
            ActiveNote(
                id = NoteId(UUID.randomUUID()),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                reminderAt = ReminderAt(reminderAt),
                createdAt = Instant.ofEpochMilli(1_000),
                updatedAt = Instant.ofEpochMilli(2_000)
            )
        )

        assertEquals(3_000L, noteDao.notes.single().reminderAtMillis)
    }

    @Test
    fun observe_mapsReminderToActiveNoteOnly() = runTest {
        val activeNoteId = UUID.randomUUID()
        val archivedNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = activeNoteId, reminderAtMillis = 3_000),
                    noteEntity(
                        id = archivedNoteId,
                        reminderAtMillis = 4_000,
                        status = "ARCHIVED",
                        archivedAtMillis = 5_000
                    )
                )
            ),
            EmptyLogger
        )

        val notes = repository.observe().first()

        assertEquals(
            ReminderAt(Instant.ofEpochMilli(3_000)),
            (notes.first { it.id == NoteId(activeNoteId) } as ActiveNote).reminderAt
        )
        assertEquals(ArchivedNote::class, notes.first { it.id == NoteId(archivedNoteId) }::class)
    }

    @Test
    fun load_returnsActiveNoteById() = runTest {
        val noteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(mutableListOf(noteEntity(id = noteId, title = "Groceries"))),
            EmptyLogger
        )

        val note = repository.load(NoteId(noteId))

        assertEquals(
            ActiveNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH
            ),
            note
        )
    }

    @Test
    fun load_returnsArchivedNoteById() = runTest {
        val noteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(
                        id = noteId,
                        title = "Groceries",
                        status = "ARCHIVED",
                        archivedAtMillis = 3_000
                    )
                )
            ),
            EmptyLogger
        )

        val note = repository.load(NoteId(noteId))

        assertEquals(
            ArchivedNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                archivedAt = Instant.ofEpochMilli(3_000)
            ),
            note
        )
    }

    @Test
    fun load_returnsDiscardedNoteById() = runTest {
        val noteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(
                        id = noteId,
                        title = "Groceries",
                        status = "DISCARDED",
                        discardedAtMillis = 4_000
                    )
                )
            ),
            EmptyLogger
        )

        val note = repository.load(NoteId(noteId))

        assertEquals(
            DiscardedNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                discardedAt = Instant.ofEpochMilli(4_000)
            ),
            note
        )
    }

    @Test
    fun loadActive_returnsOnlyActiveNotes() = runTest {
        val activeNoteId = UUID.randomUUID()
        val archivedNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = activeNoteId),
                    noteEntity(
                        id = archivedNoteId,
                        status = "ARCHIVED",
                        archivedAtMillis = 3_000
                    )
                )
            ),
            EmptyLogger
        )

        assertEquals(ActiveNote::class, repository.loadActive(NoteId(activeNoteId))!!::class)
        assertEquals(null, repository.loadActive(NoteId(archivedNoteId)))
    }

    @Test
    fun loadArchived_returnsOnlyArchivedNotes() = runTest {
        val activeNoteId = UUID.randomUUID()
        val archivedNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = activeNoteId),
                    noteEntity(
                        id = archivedNoteId,
                        status = "ARCHIVED",
                        archivedAtMillis = 3_000
                    )
                )
            ),
            EmptyLogger
        )

        assertEquals(null, repository.loadArchived(NoteId(activeNoteId)))
        assertEquals(ArchivedNote::class, repository.loadArchived(NoteId(archivedNoteId))!!::class)
    }

    @Test
    fun loadDiscarded_returnsOnlyDiscardedNotes() = runTest {
        val activeNoteId = UUID.randomUUID()
        val discardedNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = activeNoteId),
                    noteEntity(
                        id = discardedNoteId,
                        status = "DISCARDED",
                        discardedAtMillis = 4_000
                    )
                )
            ),
            EmptyLogger
        )

        assertEquals(null, repository.loadDiscarded(NoteId(activeNoteId)))
        assertEquals(
            DiscardedNote::class,
            repository.loadDiscarded(NoteId(discardedNoteId))!!::class
        )
    }

    @Test
    fun save_persistsArchivedNoteEntity() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao, EmptyLogger)
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
    fun save_persistsDiscardedNoteEntity() = runTest {
        val noteDao = InMemoryNoteDao()
        val repository = RoomNoteRepository(noteDao, EmptyLogger)
        val noteId = UUID.randomUUID()

        repository.save(
            DiscardedNote(
                id = NoteId(noteId),
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.ofEpochMilli(1_000),
                updatedAt = Instant.ofEpochMilli(2_000),
                discardedAt = Instant.ofEpochMilli(4_000)
            )
        )

        assertEquals(
            noteEntity(
                id = noteId,
                title = "Groceries",
                description = "Buy coffee",
                reminderAtMillis = null,
                status = "DISCARDED",
                archivedAtMillis = null,
                discardedAtMillis = 4_000,
                createdAtMillis = 1_000,
                updatedAtMillis = 2_000
            ),
            noteDao.notes.single()
        )
    }

    @Test
    fun observe_mapsDiscardedNotes() = runTest {
        val noteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                mutableListOf(
                    noteEntity(id = noteId, status = "DISCARDED", discardedAtMillis = 4_000)
                )
            ),
            EmptyLogger
        )

        val notes = repository.observe().first()

        assertEquals(DiscardedNote::class, notes.single()::class)
        assertEquals(Instant.ofEpochMilli(4_000), (notes.single() as DiscardedNote).discardedAt)
    }

    @Test
    fun delete_removesNoteEntity() = runTest {
        val noteId = UUID.randomUUID()
        val noteDao = InMemoryNoteDao(
            mutableListOf(
                noteEntity(id = noteId)
            )
        )
        val repository = RoomNoteRepository(noteDao, EmptyLogger)

        repository.delete(NoteId(noteId))

        assertEquals(emptyList<NoteEntity>(), noteDao.notes)
    }

    @Test
    fun deleteDiscardedBefore_removesOnlyDiscardedNotesAtOrBeforeCutoff() = runTest {
        val expiredDiscardedNote = noteEntity(
            status = "DISCARDED",
            discardedAtMillis = 1_000
        )
        val cutoffDiscardedNote = noteEntity(
            status = "DISCARDED",
            discardedAtMillis = 2_000
        )
        val retainedDiscardedNote = noteEntity(
            status = "DISCARDED",
            discardedAtMillis = 2_001
        )
        val activeNote = noteEntity(discardedAtMillis = 1_000)
        val archivedNote = noteEntity(
            status = "ARCHIVED",
            archivedAtMillis = 1_000,
            discardedAtMillis = 1_000
        )
        val noteDao = InMemoryNoteDao(
            mutableListOf(
                expiredDiscardedNote,
                cutoffDiscardedNote,
                retainedDiscardedNote,
                activeNote,
                archivedNote
            )
        )
        val repository = RoomNoteRepository(noteDao, EmptyLogger)

        repository.deleteDiscardedBefore(Instant.ofEpochMilli(2_000))

        assertEquals(listOf(retainedDiscardedNote, activeNote, archivedNote), noteDao.notes)
    }

    private class InMemoryNoteDao(val notes: MutableList<NoteEntity> = mutableListOf()) : NoteDao {
        override fun observe(): Flow<List<NoteEntity>> = flowOf(notes)

        override suspend fun loadAll(): List<NoteEntity> = notes

        override suspend fun load(id: UUID): NoteEntity? = notes.firstOrNull { it.id == id }

        override suspend fun upsert(note: NoteEntity) {
            notes.removeAll { it.id == note.id }
            notes += note
        }

        override suspend fun delete(id: UUID) {
            notes.removeAll { it.id == id }
        }

        override suspend fun deleteDiscardedBefore(cutoffMillis: Long) {
            notes.removeAll { note ->
                note.status == "DISCARDED" &&
                    (note.discardedAtMillis ?: Long.MAX_VALUE) <= cutoffMillis
            }
        }
    }

    private fun noteEntity(
        id: UUID = UUID.randomUUID(),
        title: String = "",
        description: String = "Buy coffee",
        reminderAtMillis: Long? = null,
        status: String = "ACTIVE",
        archivedAtMillis: Long? = null,
        discardedAtMillis: Long? = null,
        createdAtMillis: Long = 0,
        updatedAtMillis: Long = 0
    ): NoteEntity = NoteEntity(
        id = id,
        title = title,
        description = description,
        reminderAtMillis = reminderAtMillis,
        status = status,
        archivedAtMillis = archivedAtMillis,
        discardedAtMillis = discardedAtMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}
