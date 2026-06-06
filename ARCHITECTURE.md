# Noted Architecture

## App

Noted is an Android-only app for quick notes.

Main feature: each note can optionally have an exact reminder that triggers a notification at the
selected date/time.

## Product Scope

- Single homepage screen for notes.
- Full-screen editor for creating/editing notes.
- Notes can be filtered by tags.
- Notes can be filtered to show archived notes.
- No sync.
- No backend.

## Note Model

A note has:

- Optional title.
- Mandatory description.
- Optional reminder.
- Optional tags.
- Archive state.

The domain model uses a sum type:

```text
Note = ActiveNote | ArchivedNote
```

Archived notes do not have reminders in the domain model.

## Technology Stack

- Kotlin.
- Jetpack Compose.
- Room.
- Coroutines + Flow.
- AlarmManager exact alarms.
- BroadcastReceiver notifications.
- Manual dependency injection.

## Android Version

Minimum Android version: 15.

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
    Tag.kt
    TagId.kt
    NoteTitle.kt
    NoteDescription.kt
    TagName.kt
    Reminder.kt
    NoteRepository.kt
    ReminderScheduler.kt
    Clock.kt

  features/
    CreateNote.kt
    UpdateNote.kt
    ArchiveNote.kt
    DeleteNote.kt
    Notes.kt

  infrastructure/
    NotedDatabase.kt
    NoteEntity.kt
    TagEntity.kt
    NoteTagEntity.kt
    NoteDao.kt
    TagDao.kt
    RoomNoteRepository.kt
    UuidConverter.kt
    AlarmReminderScheduler.kt
    ReminderReceiver.kt
    ReminderNotification.kt
    ReminderBootReceiver.kt
    AndroidClock.kt

  ui/
    HomeScreen.kt
    HomeViewModel.kt
    NoteEditorScreen.kt

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
@JvmInline value class TagId(val value: UUID)
@JvmInline value class NoteTitle(val value: String)
@JvmInline value class TagName(val value: String)
```

Use smart constructors for validated values.

`NoteDescription` must be non-empty after trimming.

Use `Result` constructors for values that can fail validation without throwing.

Use UUIDs for note and tag IDs.

Room stores UUIDs as strings with a converter.

## Features

Use `features/` instead of `usecase/`.

Features are app actions.

Examples:

- `CreateNote`.
- `UpdateNote`.
- `ArchiveNote`.
- `DeleteNote`.
- `Notes`.

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
- Feature classes.

## Persistence

Room can use a flat storage model even if the domain model is safer.

Example persistence shape:

```text
NoteEntity
  id: String
  title: String?
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

When reading from Room, invalid note rows are skipped instead of crashing the notes stream.

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

Reminder lifecycle:

- Archive note: cancel reminder.
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

- App title.
- Settings entry.
- Tag filter row.
- Archived filter.
- Note list.
- Add note action.

Editor:

- Full-screen note editor.
- Optional title field.
- Mandatory description field.
- Optional reminder picker.
- Optional tags.
- Save action disabled or rejected if description is empty.

## Testing Focus

- Note description validation.
- Active note archive transition.
- Archived notes cannot have reminders in domain.
- Archive cancels reminder.
- Delete cancels reminder.
- Reminder update reschedules alarm.
- Tag filtering.
- Archived filtering.
- Reboot restore schedules only active future reminders.

## Post-MVP

Import/export is intentionally excluded from MVP.

Keep UUID IDs from day one so import/export can be added later without changing the identity model.

Future import/export rules:

- Use versioned JSON.
- Export notes, tags, and note-tag relationships.
- Preserve UUIDs on export.
- Preserve imported UUIDs when there is no conflict.
- Generate a new UUID if imported note ID conflicts with an existing note.
- Reuse existing tags by name.
- Preserve imported tag UUID when name does not exist and UUID does not conflict.
- Generate new tag UUID if needed.
- Remap note-tag relationships after conflict resolution.
- Expired imported reminders are removed.
- Archived imported notes have no reminders.
- Future imported reminders require permissions.
- If permissions are denied during import, import notes without reminders.
