# Noted Architecture

## App

Noted is an Android-only app for quick notes.

Main feature: each note can optionally have an exact reminder that triggers a notification at the
selected date/time.

## Product Scope

- Single homepage screen for notes.
- Full-screen editor for creating/editing notes.
- Notes can be filtered to show archived notes.
- Archived notes can be opened read-only, deleted, or unarchived.
- No sync.
- No backend.

## Note Model

A note has:

- Title that may be empty.
- Description that may be empty.
- Optional reminder.
- Archive state.

The domain model uses a sum type:

```text
Note = ActiveNote | ArchivedNote
```

Archived notes do not have reminders in the domain model.

Archived notes can be restored to active notes. Restored notes have no reminder because archiving
cancels and clears reminder state.

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
    UnarchiveNote.kt
    DeleteNote.kt
    Notes.kt
    SetNoteReminder.kt
    ClearNoteReminder.kt
    RestoreReminders.kt

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

  ui/
    HomeScreen.kt
    EditorRoute.kt
    HomeViewModel.kt
    NoteEditorScreen.kt
    ArchivedNoteDetailsScreen.kt
    ReminderPickerDialog.kt
    ReminderDateTimeFormatter.kt
    SaveReminder.kt
    SaveReminderRequest.kt
    ReminderPermission.kt

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
```

## Newtypes

Use Kotlin value classes for meaningful values.

Examples:

```kotlin
@JvmInline value class NoteId(val value: UUID)
@JvmInline value class NoteTitle(val value: String)
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
- `UnarchiveNote`.
- `DeleteNote`.
- `Notes`.
- `SetNoteReminder`.
- `ClearNoteReminder`.
- `RestoreReminders`.

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

## Persistence

Room can use a flat storage model even if the domain model is safer.

Example persistence shape:

```text
NoteEntity
  id: UUID
  title: String
  description: String
  reminderAtMillis: Long?
  status: ACTIVE | ARCHIVED
  archivedAtMillis: Long?
  createdAtMillis: Long
  updatedAtMillis: Long
```

The repository maps Room entities to domain types:

```text
ACTIVE -> ActiveNote
ARCHIVED -> ArchivedNote
```

Entity-to-domain mapping returns `Result`.

When reading from Room, invalid note rows are logged and skipped instead of crashing the notes stream.

Archived domain notes do not expose a reminder.

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
- Delete note: cancel reminder.
- Remove reminder: cancel alarm.
- Change reminder: cancel old alarm and schedule new alarm.
- Reboot device: restore future active reminders.

## Exact Alarm Permission UX

Do not ask permissions on app start.

Ask only when user sets a reminder.

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

- Archived filter.
- Note list.
- Add note action.
- Add note action is hidden while archived filter is selected.

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
- Delete immediately for MVP.

Archived note details:

- Full-screen read-only details.
- Opened from archived note cards.
- Show title and description only.
- Do not show archived date.
- Do not allow title or description edits.
- Reminder action is not available.
- Archive action is not available.
- Delete action is available.
- Delete immediately.
- Unarchive action is available.
- Unarchive restores the note as active with no reminder.
- After unarchive, open the restored note in the editable note editor.

## Testing Focus

- Note description validation.
- Active note archive transition.
- Archived notes cannot have reminders in domain.
- Archive cancels reminder.
- Delete cancels reminder.
- Reminder update reschedules alarm.
- Archived filtering.
- Archived note details are read-only.
- Unarchive restores an archived note as active with no reminder.
- Reboot restore schedules only active future reminders.

## Post-MVP

Import/export is intentionally excluded from MVP.

Keep UUID IDs from day one so import/export can be added later without changing the identity model.

Tags are intentionally excluded from MVP.

Trash/restore deleted notes is intentionally excluded from MVP.

Auto-delete for notes where title and description are both empty is intentionally excluded from MVP.

Future deleted-note rules:

- Add trash for deleted notes.
- Restore deleted notes from trash.
- Auto-delete empty notes if desired.

Future tag rules:

- Add tags to notes.
- Filter notes by tags.

Future import/export rules:

- Use versioned JSON.
- Export notes.
- Preserve UUIDs on export.
- Preserve imported UUIDs when there is no conflict.
- Generate a new UUID if imported note ID conflicts with an existing note.
- Expired imported reminders are removed.
- Archived imported notes have no reminders.
- Future imported reminders require permissions.
- If permissions are denied during import, import notes without reminders.
