package com.cosimomatteini.noted

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.infrastructure.ReminderAlarm
import com.cosimomatteini.noted.ui.ArchivedNoteDetailsRoute
import com.cosimomatteini.noted.ui.EditorRoute
import com.cosimomatteini.noted.ui.HomeRoute
import com.cosimomatteini.noted.ui.HomeViewModel
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.util.UUID
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: NotedAppContainer
    private var noteToOpenFromNotification by mutableStateOf<NoteId?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = NotedAppContainer(applicationContext)
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
    val coroutineScope = rememberCoroutineScope()
    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(appContainer.notes) as T
        }
    )

    fun showHome() {
        screen = NotedScreen.Home
    }

    fun editNote(note: ActiveNote) {
        screen = NotedScreen.EditNote(note)
    }

    fun openArchivedNote(note: ArchivedNote) {
        screen = NotedScreen.ArchivedNoteDetails(note)
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
            onOpenArchivedNote = ::openArchivedNote
        )

        is NotedScreen.EditNote -> EditorRoute(
            appContainer = appContainer,
            activity = activity,
            note = currentScreen.note,
            onDone = ::showHome
        )

        is NotedScreen.ArchivedNoteDetails -> ArchivedNoteDetailsRoute(
            note = currentScreen.note,
            onBack = ::showHome,
            onUnarchive = {
                appContainer.unarchiveNote(currentScreen.note.id).getOrNull()?.let(::editNote)
                    ?: showHome()
            },
            onDelete = {
                appContainer.deleteNote(currentScreen.note.id)
                showHome()
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

    data class EditNote(val note: ActiveNote) : NotedScreen

    data class ArchivedNoteDetails(val note: ArchivedNote) : NotedScreen
}

private fun Intent.notificationNote(): NoteId? = getStringExtra(ReminderAlarm.EXTRA_NOTE_ID)
    ?.let { runCatching { NoteId(UUID.fromString(it)) }.getOrNull() }
