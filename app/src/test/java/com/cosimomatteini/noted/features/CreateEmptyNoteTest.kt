package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateEmptyNoteTest {
    @Test
    fun createEmptyNote_savesEmptyNote() = runTest {
        val repository = InMemoryNoteRepository()
        val now = Instant.EPOCH
        val createEmptyNote = CreateEmptyNote(repository, FixedClock(now))

        createEmptyNote()

        val savedNote = repository.savedNotes.single()
        assertEquals("", savedNote.title.value)
        assertEquals("", savedNote.description.value)
        assertEquals(now, savedNote.createdAt)
        assertEquals(now, savedNote.updatedAt)
    }

    private class InMemoryNoteRepository : NoteRepository {
        val savedNotes = mutableListOf<ActiveNote>()

        override fun observe(): Flow<List<Note>> = flowOf(savedNotes)

        override suspend fun load(id: NoteId): ActiveNote? = savedNotes.firstOrNull { it.id == id }

        override suspend fun save(note: ActiveNote) {
            savedNotes += note
        }

        override suspend fun delete(id: NoteId) {
            savedNotes.removeAll { it.id == id }
        }
    }

    private class FixedClock(
        private val now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }
}
