package com.cosimomatteini.noted.infrastructure

import com.cosimomatteini.noted.domain.NoteId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class RoomNoteRepositoryTest {
    @Test
    fun observe_skipsInvalidNotes() = runTest {
        val validNoteId = UUID.randomUUID()
        val repository = RoomNoteRepository(
            InMemoryNoteDao(
                listOf(
                    noteEntity(description = ""),
                    noteEntity(status = "UNKNOWN"),
                    noteEntity(id = validNoteId),
                    noteEntity(status = "ARCHIVED", archivedAtMillis = null),
                ),
            ),
        )

        val notes = repository.observe().first().map { it.id }

        assertEquals(listOf(NoteId(validNoteId)), notes)
    }

    private class InMemoryNoteDao(
        private val notes: List<NoteEntity>,
    ) : NoteDao {
        override fun observe(): Flow<List<NoteEntity>> = flowOf(notes)
    }

    private fun noteEntity(
        id: UUID = UUID.randomUUID(),
        description: String = "Buy coffee",
        status: String = "ACTIVE",
        archivedAtMillis: Long? = null,
    ): NoteEntity = NoteEntity(
        id = id,
        title = null,
        description = description,
        reminderAtMillis = null,
        status = status,
        archivedAtMillis = archivedAtMillis,
        createdAtMillis = 0,
        updatedAtMillis = 0,
    )
}
