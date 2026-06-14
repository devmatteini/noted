package com.cosimomatteini.noted.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                onBack(title.text, description.text)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NoteTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = "Title",
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 24.sp
                ),
                singleLine = true
            )
            NoteTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = "Note",
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionIcon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = "Archive note",
                    onClick = {
                        coroutineScope.launch {
                            onArchive(title.text, description.text)
                        }
                    }
                )
                ActionIcon(
                    imageVector = Icons.Filled.AddAlert,
                    contentDescription = "Set reminder",
                    onClick = { showReminderPicker = true }
                )
                ActionIcon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete note",
                    onClick = {
                        coroutineScope.launch {
                            onDelete()
                        }
                    }
                )
            }
            Text(
                text = reminderAt?.let { reminderAt ->
                    "Reminder: ${reminderAt.formatReminderDateTime()}"
                } ?: "No reminder",
                modifier = Modifier.fillMaxWidth()
            )
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

@Composable
private fun NoteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean = false
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = singleLine,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        style = textStyle
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ActionIcon(imageVector: ImageVector, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
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
