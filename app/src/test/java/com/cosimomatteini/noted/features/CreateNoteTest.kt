package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateNoteTest {
    @Test
    fun createNote_savesValidNote() = runTest {
        val repository = InMemoryNoteRepository()
        val now = Instant.parse("2026-06-06T10:00:00Z")
        val createNote = CreateNote(repository, FixedClock(now))

        val result = createNote(
            title = "Groceries",
            description = "Buy coffee",
        )

        assertTrue(result.isSuccess)
        val savedNote = repository.savedNotes.single()
        assertEquals("Groceries", savedNote.title?.value)
        assertEquals("Buy coffee", savedNote.description.value)
        assertEquals(now, savedNote.createdAt)
        assertEquals(now, savedNote.updatedAt)
    }

    @Test
    fun createNote_rejectsInvalidNote() = runTest {
        val repository = InMemoryNoteRepository()
        val createNote = CreateNote(repository, FixedClock(Instant.EPOCH))

        val result = createNote(
            title = "Groceries",
            description = "",
        )

        assertFalse(result.isSuccess)
        assertTrue(repository.savedNotes.isEmpty())
    }

    private class InMemoryNoteRepository : NoteRepository {
        val savedNotes = mutableListOf<ActiveNote>()

        override fun observe(): Flow<List<Note>> = flowOf(savedNotes)

        override suspend fun save(note: ActiveNote) {
            savedNotes += note
        }
    }

    private class FixedClock(
        private val now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }
}
