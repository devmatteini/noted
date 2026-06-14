package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {
    @Test
    fun visibleNotes_returnsActiveNotesForNotesDestination() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()

        val notes = visibleNotes(listOf(activeNote, archivedNote), HomeDestination.Notes)

        assertEquals(listOf(activeNote), notes)
    }

    @Test
    fun visibleNotes_returnsArchivedNotesForArchiveDestination() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()

        val notes = visibleNotes(listOf(activeNote, archivedNote), HomeDestination.Archive)

        assertEquals(listOf(archivedNote), notes)
    }

    @Test
    fun showCreateNoteAction_returnsTrueForNotesDestination() {
        assertTrue(showCreateNoteAction(HomeDestination.Notes))
    }

    @Test
    fun showCreateNoteAction_returnsFalseForArchiveDestination() {
        assertFalse(showCreateNoteAction(HomeDestination.Archive))
    }

    private fun activeNote(): ActiveNote = ActiveNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Active"),
        description = NoteDescription.of("Active note"),
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

    private fun archivedNote(): ArchivedNote = ArchivedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Archived"),
        description = NoteDescription.of("Archived note"),
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        archivedAt = Instant.EPOCH
    )
}
