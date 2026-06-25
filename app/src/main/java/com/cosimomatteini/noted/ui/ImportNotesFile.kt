package com.cosimomatteini.noted.ui

import android.Manifest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.features.BackupError
import com.cosimomatteini.noted.features.ImportedNotes
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun rememberImportNotesFile(
    activity: ComponentActivity,
    parseNotesBackupFile: (String) -> Result<List<Note>>,
    importNotes: suspend (List<Note>) -> Result<ImportedNotes>,
    now: () -> Instant,
    onMessage: (String) -> Unit
): () -> Unit {
    var pendingImportNotes by remember { mutableStateOf<List<Note>?>(null) }
    var requestImportNotificationPermission by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    fun importParsedNotes(notes: List<Note>) {
        coroutineScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { importNotes(notes).getOrThrow() }
            }.onSuccess { importedNotes ->
                onMessage("Imported ${importedNotes.count} notes")
            }.onFailure { failure ->
                onMessage(failure.importFailureMessage())
            }
        }
    }

    fun handleParsedImportNotes(notes: List<Note>) {
        when (
            nextImportPermissionAction(
                notes = notes,
                now = now(),
                state = activity.reminderPermissionState()
            )
        ) {
            ImportPermissionAction.Import -> importParsedNotes(notes)
            ImportPermissionAction.RequestNotification -> {
                pendingImportNotes = notes
                activity.markNotificationPermissionRequested()
                requestImportNotificationPermission = true
            }

            ImportPermissionAction.OpenNotificationSettings -> {
                onMessage("Enable notifications to import reminders")
                activity.openNotificationSettings()
            }

            ImportPermissionAction.OpenExactAlarmSettings -> {
                onMessage("Enable exact alarms to import reminders")
                activity.openExactAlarmSettings()
            }
        }
    }

    fun handleImportContent(content: String) {
        val notes = parseNotesBackupFile(content).getOrElse { failure ->
            onMessage(failure.importFailureMessage())
            return
        }
        handleParsedImportNotes(notes)
    }

    val importNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val notes = pendingImportNotes
        pendingImportNotes = null
        if (granted && notes != null) {
            handleParsedImportNotes(notes)
        } else {
            onMessage("Import requires notification permission")
        }
    }

    LaunchedEffect(requestImportNotificationPermission) {
        if (requestImportNotificationPermission) {
            requestImportNotificationPermission = false
            importNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val importDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    activity.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.reader().use { reader -> reader.readText() }
                    } ?: error("Could not open import file.")
                }
            }.onSuccess { content ->
                handleImportContent(content)
            }.onFailure {
                onMessage("Import failed")
            }
        }
    }

    return { importDocumentLauncher.launch(arrayOf("application/json")) }
}

private fun Throwable.importFailureMessage(): String = when (this) {
    is BackupError.UnsupportedVersion -> "Unsupported backup version"
    is BackupError.MalformedBackup -> "Malformed backup file"
    else -> "Import failed"
}
