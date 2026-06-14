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
    fun visibleNotes_returnsActiveNotesForActiveFilter() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()

        val notes = visibleNotes(listOf(activeNote, archivedNote), HomeFilter.Active)

        assertEquals(listOf(activeNote), notes)
    }

    @Test
    fun visibleNotes_returnsArchivedNotesForArchivedFilter() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()

        val notes = visibleNotes(listOf(activeNote, archivedNote), HomeFilter.Archived)

        assertEquals(listOf(archivedNote), notes)
    }

    @Test
    fun showCreateNoteAction_returnsTrueForActiveFilter() {
        assertTrue(showCreateNoteAction(HomeFilter.Active))
    }

    @Test
    fun showCreateNoteAction_returnsFalseForArchivedFilter() {
        assertFalse(showCreateNoteAction(HomeFilter.Archived))
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
