package com.cosimomatteini.noted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.ui.HomeRoute
import com.cosimomatteini.noted.ui.HomeViewModel
import com.cosimomatteini.noted.ui.NoteEditorScreen
import com.cosimomatteini.noted.ui.SaveReminderRequest
import com.cosimomatteini.noted.ui.rememberSaveReminder
import com.cosimomatteini.noted.ui.theme.NotedTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: NotedAppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = NotedAppContainer(applicationContext)
        enableEdgeToEdge()
        setContent {
            NotedTheme {
                NotedApp(appContainer)
            }
        }
    }
}

@Composable
fun NotedApp(appContainer: NotedAppContainer) {
    var screen by remember { mutableStateOf<NotedScreen>(NotedScreen.Home) }
    val coroutineScope = rememberCoroutineScope()
    val saveReminder = rememberSaveReminder(appContainer.setNoteReminder::invoke)
    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(appContainer.notes) as T
        }
    )

    when (val currentScreen = screen) {
        NotedScreen.Home -> HomeRoute(
            viewModel = homeViewModel,
            onCreateNote = {
                coroutineScope.launch {
                    val note = appContainer.createEmptyNote()
                    screen = NotedScreen.EditNote(note)
                }
            },
            onEditNote = { note -> screen = NotedScreen.EditNote(note) }
        )

        is NotedScreen.EditNote -> NoteEditorScreen(
            initialTitle = currentScreen.note.title.value,
            initialDescription = currentScreen.note.description.value,
            initialReminderAt = currentScreen.note.reminderAt?.value,
            onAutosave = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
            },
            onBack = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
                screen = NotedScreen.Home
            },
            onArchive = { title, description ->
                appContainer.updateNote(currentScreen.note.id, title, description)
                appContainer.archiveNote(currentScreen.note.id)
                screen = NotedScreen.Home
            },
            onDelete = {
                appContainer.deleteNote(currentScreen.note.id)
                screen = NotedScreen.Home
            },
            onSetReminder = { reminderAt ->
                saveReminder(SaveReminderRequest(currentScreen.note.id, reminderAt))
            },
            onClearReminder = {
                appContainer.clearNoteReminder(currentScreen.note.id)
            }
        )
    }
}

private sealed interface NotedScreen {
    data object Home : NotedScreen

    data class EditNote(val note: ActiveNote) : NotedScreen
}
