# Noted Implementation Plan

## 1. Bootstrap Project (Done)

- Create native Android project.
- Use Kotlin.
- Use Jetpack Compose.
- Set minimum Android support to Android 15+ / API 35+.
- Configure package name.
- Add Compose dependencies.
- Add AndroidX lifecycle/ViewModel dependencies.
- Add Room dependencies.
- Add Kotlin coroutines dependencies.
- Add test dependencies.
- Enable Room schema export if desired.
- Verify app builds with an empty Compose screen.

## 2. Add Architecture Skeleton (Done)

- Create `domain/`.
- Create `features/`.
- Create `infrastructure/`.
- Create `ui/`.
- Add `NotedAppContainer.kt`.
- Add `Clock` port.
- Add `AndroidClock`.
- Wire app container into the app entry point.
- Verify app still builds.

## 3. Show Notes On Homepage (Done)

- Add `NoteId` backed by UUID.
- Add `NoteTitle`.
- Add `NoteDescription` with smart constructor and `Result` constructor.
- Add `Reminder` / `ReminderAt` only if needed for display shape.
- Add `Note` sealed interface.
- Add `ActiveNote`.
- Add `ArchivedNote`.
- Add `NoteRepository.observe` with note observation.
- Add `UuidConverter`.
- Add `NoteEntity`.
- Add `NoteDao`.
- Add `NotedDatabase`.
- Add `RoomNoteRepository` mapping entities to domain notes.
- Skip invalid database note rows when mapping to domain notes.
- Add `Notes` feature.
- Wire repository and feature in `NotedAppContainer`.
- Add `HomeViewModel`.
- Add `HomeScreen` with active notes list.
- Add empty state.
- Add tests for `NoteDescription.createResult` validation.
- Add tests for skipping invalid database note rows.
- Verify active notes render from database.

## 4. Create Note (Done)

- Add pure function or constructor path for creating active notes.
- Add repository insert/save support.
- Add `CreateNote` feature.
- Wire `CreateNote` in `NotedAppContainer`.
- Add create action on homepage.
- Add `NoteEditorScreen` create mode.
- Support optional title.
- Support mandatory description.
- Disable or reject save when description is empty.
- Return to homepage after save.
- Add tests for creating valid notes.
- Add tests rejecting empty descriptions.
- Verify created notes appear on homepage.

## 5. Edit Note (Done)

- Add repository get/update support.
- Add pure function for updating title/description.
- Add `UpdateNote` feature.
- Wire `UpdateNote` in `NotedAppContainer`.
- Open `NoteEditorScreen` in edit mode from note list.
- Pre-fill existing note fields.
- Save edits.
- Preserve note ID and created timestamp.
- Update `updatedAt`.
- Add tests for editing note content.
- Verify edits appear on homepage.

## 6. Refactor Editor To Autosave (Done)

- Refactor `NoteTitle` to trim values and allow empty strings.
- Refactor `NoteDescription` to trim values and allow empty strings.
- Make note title non-null in domain model.
- Keep note description non-null in domain model.
- Allow notes where title and description are both empty.
- Update persistence shape so title and description are non-null strings.
- Create empty notes immediately from homepage action.
- Open editor for the created note.
- Remove create/edit mode title from editor top bar.
- Remove save button.
- Autosave title/description changes after 300ms debounce.
- Back flushes pending autosave before returning home.
- Update create tests for empty notes.
- Update edit tests for empty notes.
- Update title/description tests for trimming and empty values.
- Verify created empty notes appear on homepage.

## 7. Polish Note Editor UI (Done)

- Match `docs/ui/noted-editor-screen.png`.
- Use full-screen editor layout.
- Add top bar with back icon only.
- Show title field above description field.
- Make description field fill remaining space.
- Keep bottom action row visible above navigation bar and keyboard.
- Do not add future action icons yet.
- Do not show visible icon labels.
- Add content descriptions for icon buttons.
- Verify portrait/landscape rotation keeps actions working.

## 8. Delete Note (Done)

- Add repository delete support.
- Add `DeleteNote` feature.
- Wire `DeleteNote` in `NotedAppContainer`.
- Add delete bottom action in edit mode.
- Delete immediately for MVP.
- Return to homepage after delete.
- Add tests for deleting notes.
- Verify deleted notes disappear from homepage.

## 9. Archive Note (Done)

- Add pure function to archive an active note.
- Ensure `ArchivedNote` has no reminder field.
- Store note status as active/archived.
- Add repository archive/save support if missing.
- Add `ArchiveNote` feature.
- Wire `ArchiveNote` in `NotedAppContainer`.
- Add archive bottom action in edit mode.
- Return to homepage after archive.
- Hide archived notes from default homepage view.
- Add tests for active note archive transition.
- Add tests proving archived notes have no reminder.
- Verify archived notes disappear from default homepage view.

## 10. Filter Archived Notes (Done)

- Add archived filter state to `HomeViewModel`.
- Add archived filter UI.
- Update repository observation/filtering if needed.
- Show archived notes when archived filter is enabled.
- Add tests for archived filtering.
- Verify active and archived filters work.

## 11. Add Reminder Storage (Done)

- Add reminder field to `ActiveNote` if not already present.
- Add pure function to set reminder on active note.
- Add pure function to clear reminder on active note.
- Persist `reminderAtMillis` in `NoteEntity`.
- Map reminder storage to `ActiveNote` only.
- Ensure archived notes do not expose reminders.
- Add reminder bottom action in create/edit mode.
- Add reminder picker in `NoteEditorScreen`.
- Save reminder value only after permission flow succeeds in later step.
- Add tests for setting and clearing reminders.
- Verify reminder value displays in editor and list if shown.

## 12. Add Reminder Permission UX (Done)

- Add notification permission to manifest.
- Add exact alarm permission to manifest.
- When user sets a reminder, check notification permission.
- Request notification permission if needed.
- Check exact alarm access.
- Open exact alarm settings if needed.
- Save reminder only if both permissions are granted.
- If either permission is denied, do not set reminder.
- Saving a note must not fail solely because reminder permission is denied.
- If reminder permissions fail, save note without reminder.
- Verify denied permissions do not save reminders.

## 13. Schedule And Cancel Exact Alarms (Done)

- Add `ReminderScheduler` port.
- Add `AlarmReminderScheduler`.
- Use `AlarmManager.setExactAndAllowWhileIdle()`.
- Use stable request code derived from `NoteId`.
- Schedule future reminders.
- Cancel reminders by `NoteId`.
- Avoid scheduling past reminders.
- Update `CreateNote` / `UpdateNote` to schedule reminder when present.
- Update `UpdateNote` to cancel old alarm and schedule new alarm when reminder changes.
- Update `DeleteNote` to cancel reminder.
- Update `ArchiveNote` to cancel reminder.
- Wire scheduler in `NotedAppContainer`.
- Add tests for archive cancelling reminders.
- Add tests for delete cancelling reminders.
- Add tests for reminder update rescheduling alarms.
- Verify alarms are scheduled/cancelled during note changes.

## 14. Show Reminder Notifications (Done)

- Add `ReminderReceiver`.
- Add `ReminderNotification`.
- Create notification channel.
- Register reminder receiver in manifest.
- Show note reminder notification.
- Open app/note from notification if practical.
- Verify notification appears at reminder time.

## 15. Open Reminder Notification To Note Editor (Done)

- Add note ID extra to reminder notification content intent.
- Keep using `MainActivity` as notification entrypoint.
- Teach `MainActivity` to read note ID launch intents.
- Load active note by ID on app start from notification.
- Handle `onNewIntent` when app is already running.
- Open `NoteEditorScreen` for that note.
- If note is missing, invalid, or archived, fall back to homepage.
- Preserve normal launcher behavior.
- Verify tapping reminder notification opens editor for that note.

## 16. Restore Reminders After Reboot (Done)

- Add `ReminderBootReceiver`.
- Add `RECEIVE_BOOT_COMPLETED` permission.
- Register boot receiver in manifest.
- On boot, load active notes with future reminders.
- Reschedule exact alarms.
- Add tests for reboot restore selecting only active future reminders.
- Verify reboot restore manually if feasible.
