# Noted Architecture

## Overview

Noted is an Android-only app for quick notes.

Each note can optionally have an exact reminder that triggers a notification at the selected
date/time.

## Product Features

- Single homepage for notes.
- Notes can be shown as a list or two-column grid with a persisted user preference.
- Active notes can be pinned so important notes appear first in the Notes list/grid.
- Full-screen editor for creating and editing notes.
- Notes can be filtered to show active notes, archived notes, or trash.
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
- Active-only pin flag.
- Lifecycle state.

The domain model uses a sum type:

```text
Note = ActiveNote | ArchivedNote | DiscardedNote
```

Archived and discarded notes do not have reminders in the domain model.

Archived and discarded notes do not have pins in the domain model.

## Business Rules

### Note Lifecycle

- Active notes can be archived or discarded.
- Archived notes can be restored to active notes.
- Archived notes can be discarded.
- Discarded notes can be restored to active notes.
- Discarded notes can be permanently deleted.
- Permanently delete is only allowed for discarded notes.
- Restored archived notes have no reminder.
- Restored discarded notes have no reminder.
- Discarded notes are retained for exactly 30 days from `discardedAt`.
- Expired discarded notes are permanently deleted on app open.

### Pin Rules

- Pins are available only on active notes.
- Multiple active notes can be pinned.
- Pinned active notes sort before unpinned active notes.
- Within pinned and unpinned groups, active notes keep newest-updated-first ordering.
- Archiving an active note removes its pin.
- Discarding an active note removes its pin.
- Restored archived notes are not pinned.
- Restored discarded notes are not pinned.

### Reminder Rules

- Reminders are available only on active notes.
- Reminders require notification permission and exact alarm access.
- If reminder permissions are denied, no reminder is saved.
- Saving note content must not fail solely because reminder permission is denied.
- Archiving an active note cancels its reminder.
- Discarding an active note cancels its reminder.
- Removing a reminder cancels its alarm.
- Changing a reminder cancels the old alarm and schedules the new alarm.
- Reboot restore schedules only active notes with future reminders.
- Import cancels alarms for imported active notes, then schedules imported active future reminders.

### Import/Export Rules

- Backups use versioned JSON.
- Schema version 1 stores exported time, note IDs, lifecycle state, note timestamps, and
  state-specific reminder/archive/discard timestamps.
- Export includes all lifecycle states.
- Import parses and validates the whole file before writing.
- If import parsing fails, no database writes or alarm changes happen.
- Duplicate note IDs inside one backup file keep the last note.
- Imported notes preserve UUIDs and timestamps.
- If an imported UUID already exists in the database, the imported note replaces that database note.
- Import writes already parsed domain notes. It does not parse JSON.

## UX

### UI

- Use outlined icons by default.
- Use non-outlined icons only when they communicate state, such as selected or active.

### Homepage

- Homepage has Notes, Archive, and Trash destinations.
- Notes sorts pinned notes first, then by most recent updated notes
- Pinned note cards use a different border.
- Add note action is visible only on the Notes destination.
- Top-bar overflow has Export and Import actions.
- Trash empty state title is `No notes in the trash`.

### Editor

- Creating a note immediately persists an empty note and opens the editor.
- Title and description autosave after 300ms debounce.
- Back flushes pending autosave before returning home.
- Title and description may be empty.
- Reminder, archive, and delete actions are available for active notes.
- Pin and unpin actions are available for active notes.
- Delete discards the note into trash.

### Read-Only Details

- Archived and discarded notes open in read-only details screens.
- Read-only details show title and description only.
- Read-only details do not show archive or discard timestamps.
- Archived note details allow unarchive and discard.
- Discarded note details allow restore and permanent delete.
- Restore or unarchive opens the restored active note in the editor.

### Reminder Permission

- Do not ask permissions on app start.
- Ask only when the user sets a reminder or imports notes with active future reminders.
- If notification permission can be requested, request it.
- If notification permission cannot be requested, open notification settings.
- If exact alarm access is missing, open exact alarm settings.
- Manual reminder save succeeds only when both permissions are granted.
- Import with active future reminders does not run until required permissions are granted.
- Import with no active future reminders runs without reminder permission prompts.

## Technical Decisions

### Platform And Stack

- Minimum Android support: Android 15+ / API 35+.
- Kotlin.
- Jetpack Compose.
- Room.
- Coroutines + Flow.
- Android exact alarms.
- BroadcastReceiver notifications.
- Manual dependency injection.

### Architecture Style

Use pragmatic clean/hexagonal architecture.

Keep the app simple. Avoid heavy enterprise layering.

### Directory Structure

```text
app/
  domain/          core note model and ports
  features/        app actions and business workflows
  infrastructure/  Room, alarms, notifications, platform adapters
  ui/              Compose screens and Android UI flows
```

`domain/` and `features/` are flat.

`infrastructure/` contains adapters and Android-specific implementations.

`ui/` contains screens and Android UI workflows.

### Dependency Rules

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

### Domain Modelling

Use immutable domain models.

Use simple pure functions for note transitions.

Do not use event-style modelling.

Use state names:

```text
ActiveNote
ArchivedNote
DiscardedNote
```

### Newtypes

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

Use UUIDs for note IDs.

### Features Layer

Use `features/` instead of `usecase/`.

Features are app actions.

Feature classes should stay small and express one user-visible or lifecycle action, such as
creating, editing...

### Dependency Injection

Use manual dependency injection.

Do not use Hilt unless explicitly requested.

The container wires the database, repository, reminder scheduler, clock...

### Persistence

Room can use a flat storage model even if the domain model is safer.

Persistence stores the note lifecycle in one table with nullable status-specific fields.

The repository maps persistence rows to domain types:

```text
ACTIVE -> ActiveNote
ARCHIVED -> ArchivedNote
DISCARDED -> DiscardedNote
```

When reading from Room, invalid note rows are logged and skipped instead of crashing the notes
stream.

Archived and discarded domain notes do not expose a reminder.

Archived and discarded domain notes do not expose a pin.

### Import/Export Design

Export loads all notes, encodes versioned JSON, asks the user where to save the file, and writes the
backup to the selected location.

Import asks the user to select a JSON file, reads it, parses and validates it into domain notes,
gates reminder permissions if needed, then transactionally saves notes and updates alarms for
imported active notes.

File parsing, permission gating, persistence, and alarm scheduling are separate responsibilities.

### Reminder Platform

Use Android exact reminders that can fire while idle.

Alarms can be lost after device reboot, so the app listens for reboot and restores persisted active
future reminders.

## Post-MVP

Auto-delete for notes where title and description are both empty is intentionally excluded from MVP.
