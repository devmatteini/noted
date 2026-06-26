package com.cosimomatteini.noted

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.features.ExportedNotes
import com.cosimomatteini.noted.infrastructure.ReminderAlarm
import com.cosimomatteini.noted.ui.ArchivedNoteDetailsRoute
import com.cosimomatteini.noted.ui.DiscardedNoteDetailsRoute
import com.cosimomatteini.noted.ui.EditorRoute
import com.cosimomatteini.noted.ui.HomeRoute
import com.cosimomatteini.noted.ui.HomeViewModel
import com.cosimomatteini.noted.ui.SearchRoute
import com.cosimomatteini.noted.ui.rememberImportNotesFile
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: NotedAppContainer
    private var noteToOpenFromNotification by mutableStateOf<NoteId?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = NotedAppContainer(applicationContext)
        lifecycleScope.launch(Dispatchers.IO) {
            appContainer.deleteExpiredDiscardedNotes()
        }
        noteToOpenFromNotification = intent.notificationNote()
        enableEdgeToEdge()
        setContent {
            NotedTheme {
                NotedApp(
                    appContainer = appContainer,
                    activity = this,
                    noteToOpenFromNotification = noteToOpenFromNotification,
                    onNotificationNoteShown = { noteToOpenFromNotification = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        noteToOpenFromNotification = intent.notificationNote()
    }
}

@Composable
fun NotedApp(
    appContainer: NotedAppContainer,
    activity: ComponentActivity,
    noteToOpenFromNotification: NoteId? = null,
    onNotificationNoteShown: () -> Unit = {}
) {
    var screen by remember { mutableStateOf<NotedScreen>(NotedScreen.Home) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var pendingExport by remember { mutableStateOf<ExportedNotes?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val exportDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            pendingExport = null
            return@rememberLauncherForActivityResult
        }
        val export = pendingExport ?: return@rememberLauncherForActivityResult
        pendingExport = null
        coroutineScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    activity.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.writer().use { writer -> writer.write(export.json) }
                    } ?: error("Could not open export file.")
                }
            }.onSuccess {
                notificationMessage = "Notes exported"
            }.onFailure {
                notificationMessage = "Export failed"
            }
        }
    }
    val importNotesFile = rememberImportNotesFile(
        activity = activity,
        parseNotesBackupFile = appContainer.parseNotesBackupFile::invoke,
        importNotes = appContainer.importNotes::invoke,
        now = appContainer.clock::now,
        onMessage = { message -> notificationMessage = message }
    )
    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(
                notes = appContainer.notes,
                notesLayoutPreference = appContainer.notesLayoutPreference
            ) as T
        }
    )

    fun showHome() {
        screen = NotedScreen.Home
    }

    fun showSearch() {
        screen = NotedScreen.Search
    }

    fun openSearch() {
        homeViewModel.clearSearchQuery()
        showSearch()
    }

    fun showActiveHome() {
        homeViewModel.showNotes()
        showHome()
    }

    fun editNote(note: ActiveNote, fromSearch: Boolean = false) {
        screen = NotedScreen.EditNote(note, fromSearch)
    }

    fun openArchivedNote(note: ArchivedNote, fromSearch: Boolean = false) {
        screen = NotedScreen.ArchivedNoteDetails(note, fromSearch)
    }

    fun openDiscardedNote(note: DiscardedNote, fromSearch: Boolean = false) {
        screen = NotedScreen.DiscardedNoteDetails(note, fromSearch)
    }

    fun openPreviousScreenOrHome(fromSearch: Boolean) {
        if (fromSearch) showSearch() else showHome()
    }

    fun openRestoredOrPreviousScreen(restoredNote: ActiveNote?, fromSearch: Boolean) {
        if (restoredNote == null) {
            openPreviousScreenOrHome(fromSearch)
        } else {
            editNote(restoredNote, fromSearch = fromSearch)
        }
    }

    fun createAndEditNote() {
        coroutineScope.launch {
            editNote(appContainer.createEmptyNote())
        }
    }

    NotificationOpenHandler(
        noteToOpen = noteToOpenFromNotification,
        loadNote = appContainer.noteRepository::loadActive,
        onEditNote = ::editNote,
        onShowHome = ::showHome,
        onHandled = onNotificationNoteShown
    )

    when (val currentScreen = screen) {
        NotedScreen.Home -> HomeRoute(
            viewModel = homeViewModel,
            onCreateNote = ::createAndEditNote,
            onEditNote = ::editNote,
            onOpenArchivedNote = ::openArchivedNote,
            onOpenDiscardedNote = ::openDiscardedNote,
            onOpenSearch = ::openSearch,
            onExportNotes = {
                coroutineScope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) { appContainer.exportNotes() }
                    }.onSuccess { export ->
                        pendingExport = export
                        exportDocumentLauncher.launch(export.filename)
                    }.onFailure {
                        notificationMessage = "Export failed"
                    }
                }
            },
            onImportNotes = importNotesFile,
            notificationMessage = notificationMessage,
            onNotificationMessageShown = { notificationMessage = null }
        )

        NotedScreen.Search -> SearchRoute(
            viewModel = homeViewModel,
            onBack = ::showHome,
            onEditNote = { note -> editNote(note, fromSearch = true) },
            onOpenArchivedNote = { note -> openArchivedNote(note, fromSearch = true) },
            onOpenDiscardedNote = { note -> openDiscardedNote(note, fromSearch = true) }
        )

        is NotedScreen.EditNote -> EditorRoute(
            appContainer = appContainer,
            activity = activity,
            note = currentScreen.note,
            onDone = if (currentScreen.fromSearch) ::showSearch else ::showActiveHome
        )

        is NotedScreen.ArchivedNoteDetails -> ArchivedNoteDetailsRoute(
            note = currentScreen.note,
            onBack = { openPreviousScreenOrHome(currentScreen.fromSearch) },
            onRestore = {
                val restoredNote = appContainer.restoreNote(currentScreen.note.id).getOrNull()
                openRestoredOrPreviousScreen(restoredNote, currentScreen.fromSearch)
            },
            onDelete = {
                appContainer.discardNote(currentScreen.note.id)
                openPreviousScreenOrHome(currentScreen.fromSearch)
            }
        )

        is NotedScreen.DiscardedNoteDetails -> DiscardedNoteDetailsRoute(
            note = currentScreen.note,
            onBack = { openPreviousScreenOrHome(currentScreen.fromSearch) },
            onRestore = {
                val restoredNote = appContainer.restoreDiscardedNote(
                    currentScreen.note.id
                ).getOrNull()
                openRestoredOrPreviousScreen(restoredNote, currentScreen.fromSearch)
            },
            onPermanentlyDelete = {
                appContainer.permanentlyDeleteNote(currentScreen.note.id)
                openPreviousScreenOrHome(currentScreen.fromSearch)
            }
        )
    }
}

@Composable
private fun NotificationOpenHandler(
    noteToOpen: NoteId?,
    loadNote: suspend (NoteId) -> ActiveNote?,
    onEditNote: (ActiveNote) -> Unit,
    onShowHome: () -> Unit,
    onHandled: () -> Unit
) {
    LaunchedEffect(noteToOpen) {
        val noteId = noteToOpen ?: return@LaunchedEffect
        loadNote(noteId)?.let(onEditNote) ?: onShowHome()
        onHandled()
    }
}

private sealed interface NotedScreen {
    data object Home : NotedScreen

    data object Search : NotedScreen

    data class EditNote(val note: ActiveNote, val fromSearch: Boolean = false) : NotedScreen

    data class ArchivedNoteDetails(val note: ArchivedNote, val fromSearch: Boolean = false) :
        NotedScreen

    data class DiscardedNoteDetails(val note: DiscardedNote, val fromSearch: Boolean = false) :
        NotedScreen
}

private fun Intent.notificationNote(): NoteId? = getStringExtra(ReminderAlarm.EXTRA_NOTE_ID)
    ?.let { runCatching { NoteId(UUID.fromString(it)) }.getOrNull() }
