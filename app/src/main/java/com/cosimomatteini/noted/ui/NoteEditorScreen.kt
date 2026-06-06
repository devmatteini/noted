package com.cosimomatteini.noted.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialTitle: String = "",
    initialDescription: String = "",
    initialReminderAt: Instant? = null,
    onAutosave: suspend (title: String, description: String) -> Unit,
    onBack: suspend (title: String, description: String) -> Unit,
    onArchive: suspend (title: String, description: String) -> Unit,
    onDelete: suspend () -> Unit,
    onSetReminder: suspend (ReminderAt) -> Boolean = { false },
    onClearReminder: suspend () -> Unit = {}
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
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Description") }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                ActionIcon(
                    onClick = {
                        coroutineScope.launch {
                            onArchive(title.text, description.text)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Archive,
                        contentDescription = "Archive note"
                    )
                }
                ActionIcon(
                    onClick = { showReminderPicker = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddAlert,
                        contentDescription = "Set reminder"
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable {
                            coroutineScope.launch {
                                onDelete()
                            }
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete note"
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderPickerDialog(
    initialReminderAt: ReminderAt?,
    onDismiss: () -> Unit,
    onSetReminder: (ReminderAt) -> Unit,
    onClearReminder: () -> Unit
) {
    val initialReminderDateTime = remember(initialReminderAt) {
        initialReminderAt?.value?.atZone(ZoneId.systemDefault()) ?: ZonedDateTime.now().plusHours(1)
    }
    var selectedDate by remember(initialReminderAt) {
        mutableStateOf(initialReminderDateTime.toLocalDate())
    }
    var selectedTime by remember(initialReminderAt) {
        mutableStateOf(initialReminderDateTime.toLocalTime().withSecond(0).withNano(0))
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pick date and time",
                    style = MaterialTheme.typography.headlineMedium
                )
                ReminderRow(
                    text = selectedDate.formatReminderDialogDate(),
                    onClick = { showDatePicker = true }
                )
                ReminderRow(
                    text = selectedTime.formatReminderDialogTime(),
                    onClick = { showTimePicker = true }
                )
                val selectedReminderAt = ReminderAt(
                    selectedDate
                        .atTime(selectedTime)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                )
                val canSave = selectedReminderAt.value >= Instant.now()
                if (!canSave) {
                    Text(
                        text = "Reminder must be in the future",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onClearReminder) {
                        Text("Clear")
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            onSetReminder(selectedReminderAt)
                        },
                        enabled = canSave,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        ReminderDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            }
        )
    }

    if (showTimePicker) {
        ReminderTimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            }
        )
    }
}

@Composable
private fun ReminderRow(text: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null
            )
        }
        HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toPickerMillis(),
        selectableDates = FutureReminderDates
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        onDateSelected(selectedDateMillis.toPickerDate())
                    }
                }
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit
) {
    val context = LocalContext.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = is24Hour
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = {
            TimeInput(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("Ok")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ActionIcon(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneId.of("UTC"))
    .toInstant()
    .toEpochMilli()

private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.of("UTC"))
    .toLocalDate()

@OptIn(ExperimentalMaterial3Api::class)
private object FutureReminderDates : SelectableDates {
    override fun isSelectableDate(utcTimeMillis: Long): Boolean =
        utcTimeMillis.toPickerDate() >= LocalDate.now()

    override fun isSelectableYear(year: Int): Boolean = year >= LocalDate.now().year
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
            onArchive = { _, _ -> },
            onDelete = {},
            onSetReminder = { true },
            onClearReminder = {}
        )
    }
}
