package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.features.Notes
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        kotlinx.coroutines.Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    @Test
    fun visibleNotes_returnsActiveNotesForNotesDestination() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()
        val discardedNote = discardedNote()

        val notes = visibleNotes(
            listOf(activeNote, archivedNote, discardedNote),
            HomeDestination.Notes
        )

        assertEquals(listOf(activeNote), notes)
    }

    @Test
    fun visibleNotes_returnsArchivedNotesForArchiveDestination() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()
        val discardedNote = discardedNote()

        val notes = visibleNotes(
            listOf(activeNote, archivedNote, discardedNote),
            HomeDestination.Archive
        )

        assertEquals(listOf(archivedNote), notes)
    }

    @Test
    fun visibleNotes_returnsDiscardedNotesForTrashDestination() {
        val activeNote = activeNote()
        val archivedNote = archivedNote()
        val discardedNote = discardedNote()

        val notes = visibleNotes(
            listOf(activeNote, archivedNote, discardedNote),
            HomeDestination.Trash
        )

        assertEquals(listOf(discardedNote), notes)
    }

    @Test
    fun showCreateNoteAction_returnsTrueForNotesDestination() {
        assertTrue(showCreateNoteAction(HomeDestination.Notes))
    }

    @Test
    fun showCreateNoteAction_returnsFalseForArchiveDestination() {
        assertFalse(showCreateNoteAction(HomeDestination.Archive))
    }

    @Test
    fun showCreateNoteAction_returnsFalseForTrashDestination() {
        assertFalse(showCreateNoteAction(HomeDestination.Trash))
    }

    @Test
    fun uiState_loadsPersistedListLayout() = runTest {
        val viewModel = homeViewModel(layout = NotesLayout.List)

        assertEquals(NotesLayout.List, viewModel.uiState.value.layout)
    }

    @Test
    fun uiState_loadsPersistedGridLayout() = runTest {
        val viewModel = homeViewModel(layout = NotesLayout.Grid)

        assertEquals(NotesLayout.Grid, viewModel.uiState.value.layout)
    }

    @Test
    fun toggleLayout_changesListToGridAndPersistsGrid() = runTest {
        val preference = FakeNotesLayoutPreference(NotesLayout.List)
        val viewModel = homeViewModel(preference = preference)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.toggleLayout()

        assertEquals(NotesLayout.Grid, viewModel.uiState.value.layout)
        assertEquals(NotesLayout.Grid, preference.layout)
    }

    @Test
    fun toggleLayout_changesGridToListAndPersistsList() = runTest {
        val preference = FakeNotesLayoutPreference(NotesLayout.Grid)
        val viewModel = homeViewModel(preference = preference)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }

        viewModel.toggleLayout()

        assertEquals(NotesLayout.List, viewModel.uiState.value.layout)
        assertEquals(NotesLayout.List, preference.layout)
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

    private fun discardedNote(): DiscardedNote = DiscardedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Discarded"),
        description = NoteDescription.of("Discarded note"),
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH,
        discardedAt = Instant.EPOCH
    )

    private fun homeViewModel(
        layout: NotesLayout = NotesLayout.List,
        preference: FakeNotesLayoutPreference = FakeNotesLayoutPreference(layout)
    ): HomeViewModel = HomeViewModel(
        notes = Notes(FakeNoteRepository()),
        notesLayoutPreference = preference
    )

    private class FakeNotesLayoutPreference(var layout: NotesLayout) : NotesLayoutPreference {
        override fun load(): NotesLayout = layout

        override fun save(layout: NotesLayout) {
            this.layout = layout
        }
    }

    private class FakeNoteRepository : NoteRepository {
        private val notes = MutableStateFlow<List<Note>>(emptyList())

        override fun observe(): Flow<List<Note>> = notes

        override suspend fun loadAll(): List<Note> = notes.value

        override suspend fun load(id: NoteId): Note? = null

        override suspend fun loadActive(id: NoteId): ActiveNote? = null

        override suspend fun loadArchived(id: NoteId): ArchivedNote? = null

        override suspend fun loadDiscarded(id: NoteId): DiscardedNote? = null

        override suspend fun save(note: Note) = Unit

        override suspend fun delete(id: NoteId) = Unit

        override suspend fun deleteDiscardedBefore(cutoff: Instant) = Unit
    }
}
