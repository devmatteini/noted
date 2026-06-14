package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PermanentlyDeleteNoteTest {
    @Test
    fun permanentlyDeleteNote_deletesDiscardedNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            DiscardedNote(
                id = noteId,
                title = NoteTitle.of("Discarded"),
                description = NoteDescription.of("Trash"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                discardedAt = Instant.EPOCH
            )
        )
        val permanentlyDeleteNote = PermanentlyDeleteNote(repository)

        val result = permanentlyDeleteNote(noteId)

        assertTrue(result.isSuccess)
        assertTrue(repository.notes.isEmpty())
    }

    @Test
    fun permanentlyDeleteNote_rejectsActiveNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val activeNote = ActiveNote(
            id = noteId,
            title = NoteTitle.of("Active"),
            description = NoteDescription.of("Editable"),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        )
        val repository = InMemoryNoteRepository(activeNote)
        val permanentlyDeleteNote = PermanentlyDeleteNote(repository)

        val result = permanentlyDeleteNote(noteId)

        assertTrue(result.isFailure)
        assertEquals(activeNote, repository.notes.single())
    }
}
