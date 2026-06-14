package com.cosimomatteini.noted.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NoteEditorScreen(
    initialTitle: String,
    initialDescription: String,
    initialReminderAt: Instant?,
    onAutosave: suspend (title: String, description: String) -> Unit,
    onBack: suspend (title: String, description: String) -> Unit,
    onArchive: suspend (title: String, description: String) -> Unit,
    onDelete: suspend () -> Unit,
    onSetReminder: suspend (ReminderAt) -> Boolean,
    onClearReminder: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember(initialTitle) { mutableStateOf(TextFieldValue(initialTitle)) }
    var description by remember(initialDescription) {
        mutableStateOf(TextFieldValue(initialDescription))
    }
    var lastSavedTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var lastSavedDescription by remember(initialDescription) { mutableStateOf(initialDescription) }
    var reminderAt by remember(initialReminderAt) {
        mutableStateOf(initialReminderAt?.let(::ReminderAt))
    }
    var showReminderPicker by remember { mutableStateOf(false) }

    BackHandler {
        coroutineScope.launch {
            onBack(title.text, description.text)
        }
    }

    LaunchedEffect(title.text, description.text) {
        if (title.text == lastSavedTitle && description.text == lastSavedDescription) {
            return@LaunchedEffect
        }
        delay(300.milliseconds)
        onAutosave(title.text, description.text)
        lastSavedTitle = title.text
        lastSavedDescription = description.text
    }

    NoteDetailsScaffold(
        onBack = {
            coroutineScope.launch {
                onBack(title.text, description.text)
            }
        }
    ) { innerPadding ->
        NoteDetailsContentColumn(
            innerPadding = innerPadding,
            imePadding = true
        ) {
            NoteTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Title",
                textStyle = noteTitleTextStyle(),
                singleLine = true
            )
            NoteTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Note",
                textStyle = noteDescriptionTextStyle()
            )
            reminderAt?.let { reminderAt ->
                NoteReminderChip(
                    text = reminderAt.formatReminderDateTime(),
                    onClick = { showReminderPicker = true }
                )
            }
            Spacer(Modifier.weight(1f))
            NoteActionsRow {
                NoteActionIcon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = "Archive note",
                    onClick = {
                        coroutineScope.launch {
                            onArchive(title.text, description.text)
                        }
                    }
                )
                NoteActionIcon(
                    imageVector = Icons.Filled.AddAlert,
                    contentDescription = "Set reminder",
                    onClick = { showReminderPicker = true }
                )
                NoteActionIcon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete note",
                    onClick = {
                        coroutineScope.launch {
                            onDelete()
                        }
                    }
                )
            }
        }
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            initialReminderAt = reminderAt,
            onDismiss = { showReminderPicker = false },
            onSetReminder = { selectedReminderAt ->
                coroutineScope.launch {
                    if (onSetReminder(selectedReminderAt)) {
                        reminderAt = selectedReminderAt
                        showReminderPicker = false
                    }
                }
            },
            onClearReminder = {
                coroutineScope.launch {
                    onClearReminder()
                    reminderAt = null
                    showReminderPicker = false
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenEmptyPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {},
            initialTitle = "",
            initialDescription = "",
            initialReminderAt = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenOnlyTitlePreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {},
            initialTitle = "My title",
            initialDescription = "",
            initialReminderAt = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenOnlyDescriptionPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {},
            initialTitle = "",
            initialDescription = "My long description.\nThis is very long.",
            initialReminderAt = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenTitleAndDescriptionPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {},
            initialTitle = "My title",
            initialDescription = "My long description.\nThis is very long.",
            initialReminderAt = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenReminderPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {},
            initialTitle = "My title",
            initialDescription = "My very long description!!!",
            initialReminderAt = Instant.EPOCH
        )
    }
}
