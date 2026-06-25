# Noted Architecture

## App

Noted is an Android-only app for quick notes.

Main feature: each note can optionally have an exact reminder that triggers a notification at the
selected date/time.

## Product Scope

- Single homepage screen for notes.
- Notes can be shown as a list or two-column grid with a persisted user preference.
- Full-screen editor for creating/editing notes.
- Notes can be filtered to show archived notes or trash.
- Archived notes can be opened read-only, discarded, or unarchived.
- Discarded notes can be opened read-only, restored, or permanently deleted.
- Discarded notes are automatically permanently deleted after 30 days.
- Notes can be exported to a versioned JSON backup file.
- Notes can be imported from a versioned JSON backup file.
- No sync.
- No backend.

## Note Model

A note has:

- Title that may be empty.
- Description that may be empty.
- Optional reminder.
- Lifecycle state.

The domain model uses a sum type:

```text
Note = ActiveNote | ArchivedNote | DiscardedNote
```

Archived and discarded notes do not have reminders in the domain model.

Archived notes can be restored to active notes. Restored notes have no reminder because archiving
cancels and clears reminder state.

Discarded notes are notes in the trash. Discarded notes can be restored to active notes. Restored
discarded notes have no reminder because discarding cancels and clears reminder state for active
notes.

Discarded notes are retained for 30 days from `discardedAt`, then permanently deleted by app-open
cleanup.

## Technology Stack

- Kotlin.
- Jetpack Compose.
- Room.
- Coroutines + Flow.
- AlarmManager exact alarms.
- BroadcastReceiver notifications.
- Manual dependency injection.

## Android Version

Minimum Android support: Android 15+ / API 35+.

## Architecture Style

Use pragmatic clean/hexagonal architecture.

Keep the app simple. Avoid heavy enterprise layering.

## Directory Structure

```text
app/
  domain/
    Note.kt
    ActiveNote.kt
    ArchivedNote.kt
    DiscardedNote.kt
    NoteId.kt
    NoteTitle.kt
    NoteDescription.kt
    ReminderAt.kt
    NoteRepository.kt
    ReminderScheduler.kt
    Clock.kt
    Logger.kt

  features/
    CreateEmptyNote.kt
    UpdateNote.kt
    ArchiveNote.kt
    RestoreNote.kt
    DiscardNote.kt
    RestoreDiscardedNote.kt
    PermanentlyDeleteNote.kt
    Notes.kt
    SetNoteReminder.kt
    ClearNoteReminder.kt
    RestoreReminders.kt
    DeleteExpiredDiscardedNotes.kt
    BackupJsonCodec.kt
    ExportNotes.kt
    ParseNotesBackupFile.kt
    ImportNotes.kt

  infrastructure/
    NotedDatabase.kt
    NotedDatabaseFactory.kt
    NoteEntity.kt
    NoteDao.kt
    RoomNoteRepository.kt
    UuidConverter.kt
    AlarmReminderScheduler.kt
    ReminderAlarm.kt
    ReminderNotificationReceiver.kt
    ReminderNotification.kt
    ReminderBootReceiver.kt
    AndroidClock.kt
    AndroidLogger.kt
    SharedPreferencesNotesLayoutPreference.kt

  ui/
    HomeScreen.kt
    EditorRoute.kt
    HomeViewModel.kt
    NoteEditorScreen.kt
    ArchivedNoteDetailsScreen.kt
    DiscardedNoteDetailsScreen.kt
    ReminderPickerDialog.kt
    ReminderDateTimeFormatter.kt
    SaveReminder.kt
    SaveReminderRequest.kt
    ReminderPermission.kt
    ImportReminderPermission.kt
    ImportNotesFile.kt
    NotesLayoutPreference.kt

  MainActivity.kt
  NotedAppContainer.kt
```

`domain/` is flat.

`features/` is flat.

`infrastructure/` contains adapters and Android-specific implementations.

`ui/` stays separate.

## Dependency Rules

```text
ui -> features -> domain
infrastructure -> domain
```

Do not allow:

```text
domain -> infrastructure
features -> infrastructure
```

`domain/` and `features/` should avoid Android imports.

`infrastructure/` and `ui/` can use Android libraries.

## Domain Modelling

Use immutable domain models.

Use simple pure functions for note transitions.

Do not use event-style modelling.

`NoteCreated`, `NoteArchived`, and similar event names are not part of the model.

Use state names:

```text
ActiveNote
ArchivedNote
DiscardedNote
```

## Newtypes

Use Kotlin value classes for meaningful values.

Examples:

```kotlin
@JvmInline
value class NoteId(val value: UUID)
@JvmInline
value class NoteTitle(val value: String)
```

Use smart constructors for validated values.

`NoteTitle` and `NoteDescription` trim values and allow empty strings.

Use `Result` constructors only for values that can fail validation without throwing.

Use UUIDs for note IDs.

Room stores UUIDs as strings with a converter.

## Features

Use `features/` instead of `usecase/`.

Features are app actions.

Examples:

- `CreateEmptyNote`.
- `UpdateNote`.
- `ArchiveNote`.
- `RestoreNote`.
- `DiscardNote`.
- `RestoreDiscardedNote`.
- `PermanentlyDeleteNote`.
- `DeleteExpiredDiscardedNotes`.
- `Notes`.
- `SetNoteReminder`.
- `ClearNoteReminder`.
- `RestoreReminders`.
- `DeleteExpiredDiscardedNotes`.
- `ExportNotes`.
- `ParseNotesBackupFile`.
- `ImportNotes`.

## Dependency Injection

Use manual dependency injection to start.

Do not use Hilt initially.

Use a simple composition container:

```text
NotedAppContainer.kt
```

The container wires:

- Room database.
- Repository.
- Reminder scheduler.
- Clock.
- Logger.
- Feature classes.

## Import/Export

Backups use versioned JSON in `BackupJsonCodec`.

Schema version 1 stores:

- `schemaVersion`.
- `exportedAt` as an ISO-8601 instant string.
- Notes with UUID string IDs.
- Note lifecycle state: `ACTIVE`, `ARCHIVED`, or `DISCARDED`.
- Note timestamps as ISO-8601 instant strings.
- Reminder timestamp only for active notes.
- Archive timestamp only for archived notes.
- Discard timestamp only for discarded notes.

Export flow:

```text
Home overflow Export
-> ExportNotes loads all notes
-> BackupJsonCodec encodes schema version 1 JSON
-> ActivityResultContracts.CreateDocument("application/json")
-> write JSON to selected URI
```

Import flow:

```text
Home overflow Import
-> ActivityResultContracts.OpenDocument()
-> read selected URI text
-> ParseNotesBackupFile parses and validates JSON into domain notes
-> if active future reminders exist, require reminder permissions
-> ImportNotes transactionally upserts parsed notes
-> cancel/schedule alarms for imported active notes only
```

Import parses and validates the whole file before writing. If parsing fails, no database writes or
alarm changes happen.

Duplicate note IDs inside one backup file keep the last note.

Imported notes preserve UUIDs and timestamps. If an imported UUID already exists in the database,
the imported note replaces that database note.

`ImportNotes` accepts already parsed domain notes. It does not parse JSON. This keeps file parsing,
permission gating, persistence, and alarm scheduling as separate responsibilities.

## Persistence

Room can use a flat storage model even if the domain model is safer.

Example persistence shape:

```text
NoteEntity
  id: UUID
  title: String
  description: String
  reminderAtMillis: Long?
  status: ACTIVE | ARCHIVED | DISCARDED
  archivedAtMillis: Long?
  discardedAtMillis: Long?
  createdAtMillis: Long
  updatedAtMillis: Long
```

The repository maps Room entities to domain types:

```text
ACTIVE -> ActiveNote
ARCHIVED -> ArchivedNote
DISCARDED -> DiscardedNote
```

Entity-to-domain mapping returns `Result`.

When reading from Room, invalid note rows are logged and skipped instead of crashing the notes
stream.

Archived and discarded domain notes do not expose a reminder.

## Reminder Rules

Use exact reminders only.

Use `AlarmManager.setExactAndAllowWhileIdle()`.

Reminders require:

- Notification permission.
- Exact alarm access.

When manually setting a reminder:

- If notification permission is denied, no reminder is saved.
- If exact alarm access is denied, no reminder is saved.
- If both permissions are granted, save reminder and schedule alarm.
- Saving a note must not fail solely because reminder permission is denied.
- If reminder permissions fail, save the note without the reminder.

Reminder lifecycle:

- Archive note: cancel reminder.
- Unarchive note: restore as active note with no reminder.
- Discard active note: cancel reminder.
- Discard archived note: no reminder to cancel.
- Restore discarded note: restore as active note with no reminder.
- Permanently delete note: only allowed for discarded notes.
- App open: permanently delete discarded notes whose `discardedAt` is at least 30 days old.
- Remove reminder: cancel alarm.
- Change reminder: cancel old alarm and schedule new alarm.
- Reboot device: restore future active reminders.
- Import notes: cancel alarms for imported active notes, then schedule imported active future
  reminders only.

## Trash Retention

Discarded notes are retained for exactly 30 days from `discardedAt`.

Cleanup runs once when `MainActivity` is created. It runs on `Dispatchers.IO` and does not block UI
startup. Destination changes such as switching to Notes, Archive, or Trash do not trigger cleanup.

## Exact Alarm Permission UX

Do not ask permissions on app start.

Ask only when user sets a reminder or imports notes with active future reminders.

Flow:

```text
User selects reminder
-> check notification permission
-> request notification permission if needed
-> check exact alarm access
-> open exact alarm settings if needed
-> save reminder only if both granted
```

If either permission is denied, no reminder is set.

The note itself can still be saved without a reminder.

## Import Reminder Permission UX

Do not ask reminder permissions just because the user selects an import file.

Parse and validate the import file first.

If the parsed import has no active future reminders, import without permission prompts.

If the parsed import has active future reminders:

- Require notification permission.
- Require exact alarm access.
- If notification permission can be requested, request it.
- If notification permission cannot be requested, open notification settings.
- If exact alarm access is missing, open exact alarm settings.
- Do not import until required permissions are granted.

## Reboot Restore

AlarmManager alarms can be lost after device reboot.

Include reboot restore in MVP.

Use `RECEIVE_BOOT_COMPLETED` and a boot receiver.

Flow:

```text
Device rebooted
-> ReminderBootReceiver runs
-> load active notes with future reminders from Room
-> schedule exact alarms again
```

## UI

Homepage:

- Notes, Archive, and Trash destinations.
- Note list.
- Add note action.
- Add note action is hidden outside the Notes destination.
- Trash bottom navigation item uses the delete icon.
- Top-bar overflow has Export and Import actions.

Editor:

- Full-screen note editor.
- Top bar has back icon only.
- No create/edit mode title.
- No save button.
- Creating a note immediately persists an empty note and opens the editor.
- Title and description autosave after 300ms debounce.
- Back flushes pending autosave before returning home.
- Title field may be empty.
- Description field may be empty.
- Title field is above description field.
- Description field fills remaining space.
- Bottom action row stays visible above navigation bar and keyboard.
- Icon actions have content descriptions and no visible labels.
- Reminder action is available.
- Archive action is available.
- Delete action is available.
- Delete discards the note into trash.

Archived note details:

- Full-screen read-only details.
- Opened from archived note cards.
- Show title and description only.
- Do not show archived date.
- Do not allow title or description edits.
- Reminder action is not available.
- Archive action is not available.
- Delete action is available.
- Delete discards the note into trash.
- Unarchive action is available.
- Unarchive restores the note as active with no reminder.
- After unarchive, open the restored note in the editable note editor.

Discarded note details:

- Full-screen read-only details.
- Opened from discarded note cards in Trash.
- Show title and description only.
- Do not show discarded date.
- Do not allow title or description edits.
- Reminder action is not available.
- Archive action is not available.
- Restore action is available.
- Restore action restores the note as active with no reminder.
- After restore, open the restored note in the editable note editor.
- Permanently delete action is available.
- Permanently delete action uses the delete forever icon.
- Permanently delete immediately removes the note.
- Trash empty state title is `No notes in the trash`.

## Testing Focus

- Note description validation.
- Active note archive transition.
- Active note discard transition.
- Archived note discard transition.
- Discarded note restore transition.
- Archived notes cannot have reminders in domain.
- Discarded notes cannot have reminders in domain.
- Archive cancels reminder.
- Discard active note cancels reminder.
- Permanently delete only accepts discarded notes.
- Expired discarded note cleanup deletes only notes at or older than 30 days.
- Reminder update reschedules alarm.
- Archived filtering.
- Trash filtering.
- Archived note details are read-only.
- Discarded note details are read-only.
- Unarchive restores an archived note as active with no reminder.
- Restore discarded note restores as active with no reminder.
- Reboot restore schedules only active future reminders.
- Backup JSON encodes and decodes active, archived, and discarded notes.
- Backup JSON rejects unsupported schema versions.
- Backup JSON rejects malformed or invalid lifecycle data.
- Export includes all lifecycle states.
- Import appends new notes and overrides notes with the same UUID.
- Import parses and validates notes before writing.
- Import bulk upsert is transactional.
- Import cancels alarms for imported active notes only.
- Import schedules imported active future reminders only.
- Import permission decision only requires permissions for active future reminders.

## Post-MVP

Auto-delete for notes where title and description are both empty is intentionally excluded from MVP.
