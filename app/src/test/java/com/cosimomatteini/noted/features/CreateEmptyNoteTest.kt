package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.support.FixedClock
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import java.time.Instant
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

        val savedNote = repository.notes.single()
        assertEquals("", savedNote.title.value)
        assertEquals("", savedNote.description.value)
        assertEquals(now, savedNote.createdAt)
        assertEquals(now, savedNote.updatedAt)
    }
}
