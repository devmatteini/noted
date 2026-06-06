package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.support.InMemoryNoteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.util.UUID

class DeleteNoteTest {
    @Test
    fun deleteNote_deletesNote() = runTest {
        val noteId = NoteId(UUID.randomUUID())
        val repository = InMemoryNoteRepository(
            ActiveNote(
                id = noteId,
                title = NoteTitle.of("Groceries"),
                description = NoteDescription.of("Buy coffee"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
            ),
        )
        val deleteNote = DeleteNote(repository)

        deleteNote(noteId)

        assertTrue(repository.notes.isEmpty())
    }

}
