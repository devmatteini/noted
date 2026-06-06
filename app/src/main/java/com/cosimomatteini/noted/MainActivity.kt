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
    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomeViewModel(
                    notes = appContainer.notes,
                    onCreateNoteRequested = { screen = NotedScreen.CreateNote },
                    onEditNoteRequested = { note -> screen = NotedScreen.EditNote(note) },
                ) as T
        },
    )

    when (val currentScreen = screen) {
        NotedScreen.Home -> HomeRoute(homeViewModel)
        NotedScreen.CreateNote -> NoteEditorScreen(
            screenTitle = "Create note",
            onSave = { title, description ->
                coroutineScope.launch {
                    appContainer.createNote(title, description)
                        .onSuccess { screen = NotedScreen.Home }
                }
            },
            onCancel = { screen = NotedScreen.Home },
        )
        is NotedScreen.EditNote -> NoteEditorScreen(
            screenTitle = "Edit note",
            initialTitle = currentScreen.note.title?.value.orEmpty(),
            initialDescription = currentScreen.note.description.value,
            onSave = { title, description ->
                coroutineScope.launch {
                    appContainer.updateNote(currentScreen.note.id, title, description)
                        .onSuccess { screen = NotedScreen.Home }
                }
            },
            onCancel = { screen = NotedScreen.Home },
        )
    }
}

private sealed interface NotedScreen {
    data object Home : NotedScreen

    data object CreateNote : NotedScreen

    data class EditNote(val note: ActiveNote) : NotedScreen
}
