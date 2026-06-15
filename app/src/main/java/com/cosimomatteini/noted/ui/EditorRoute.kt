package com.cosimomatteini.noted.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import com.cosimomatteini.noted.NotedAppContainer
import com.cosimomatteini.noted.domain.ActiveNote

@Composable
internal fun EditorRoute(
    appContainer: NotedAppContainer,
    activity: ComponentActivity,
    note: ActiveNote,
    onDone: () -> Unit
) {
    val saveReminder = rememberSaveReminder(activity, appContainer.setNoteReminder::invoke)

    suspend fun saveNote(title: String, description: String) {
        appContainer.updateNote(note.id, title, description)
    }

    suspend fun archiveAndClose(title: String, description: String) {
        saveNote(title, description)
        appContainer.archiveNote(note.id)
        onDone()
    }

    suspend fun deleteAndClose() {
        appContainer.discardNote(note.id)
        onDone()
    }

    NoteEditorScreen(
        initialTitle = note.title.value,
        initialDescription = note.description.value,
        initialReminderAt = note.reminderAt?.value,
        onAutosave = ::saveNote,
        onBack = onDone,
        onArchive = ::archiveAndClose,
        onDelete = ::deleteAndClose,
        onSetReminder = { reminderAt ->
            saveReminder(SaveReminderRequest(note.id, reminderAt))
        },
        onClearReminder = {
            appContainer.clearNoteReminder(note.id)
        }
    )
}
