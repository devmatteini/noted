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

## 17. Open Archived Note Details (Done)

- Add repository support for loading any note by ID.
- Keep active-only features from mutating archived notes.
- Make archived note cards clickable.
- Add archived note route/navigation state.
- Add `ArchivedNoteDetailsScreen`.
- Show archived note title and description read-only.
- Do not show archived date.
- Do not show reminder action.
- Do not show archive action.
- Add back action.
- Add delete action.
- Delete immediately.
- Return to homepage after deleting archived note.
- Add tests for loading active and archived notes by ID.
- Add tests proving active-only features reject archived notes.
- Verify archived notes open read-only from archived filter.

## 18. Unarchive Note (Done)

- Add pure function to restore an archived note to active.
- Ensure restored active note has no reminder.
- Add `UnarchiveNote` feature.
- Wire `UnarchiveNote` in `NotedAppContainer`.
- Add unarchive action to archived note details.
- Save restored note as active.
- After unarchive, open restored note in editable note editor.
- Add tests for archived note restore transition.
- Add tests for unarchiving archived notes.
- Verify unarchived notes move to active notes.
- Verify restored note opens in editor after unarchive.

## 19. Polish Archived Filter UX (Done)

- Hide homepage add note action when archived filter is selected.
- Keep add note action visible when active filter is selected.
- Add preview or UI test coverage if practical.
- Verify switching filters updates add note action visibility.

## 20. Discard Domain And Soft Delete (Done)

- Add `DiscardedNote` domain model with `discardedAt`.
- Add `ActiveNote.discard(discardedAt)` transition.
- Add `ArchivedNote.discard(discardedAt)` transition.
- Add `NoteRepository.loadDiscarded`.
- Add `DiscardNote` feature.
- Ensure `DiscardNote` supports active and archived notes.
- Ensure `DiscardNote` is idempotent for discarded notes.
- Ensure discarding active notes cancels reminders.
- Ensure discarding archived notes has no reminder to cancel.
- Add `discardedAtMillis` to `NoteEntity`.
- Keep Room database version unchanged for this development change.
- Update exported Room schema for the current version.
- Add `DISCARDED` persistence status.
- Map `DISCARDED` entities to `DiscardedNote`.
- Persist `DiscardedNote` with no reminder.
- Reject invalid discarded rows without `discardedAtMillis`.
- Update `NotedAppContainer` wiring.
- Update editor delete action to call `DiscardNote`.
- Update archived details delete action to call `DiscardNote`.
- Keep current `DeleteNote` unchanged for now.
- Add tests for domain discard transitions.
- Add tests for discarding active and archived notes.
- Add tests for discard reminder cancellation.
- Add repository tests for saving discarded notes.
- Add repository tests for loading discarded notes.
- Add repository tests for observing discarded notes.
- Verify app works after clearing emulator app storage.

## 21. Add Trash Destination (Done)

- Add `HomeDestination.Trash`.
- Add Trash bottom navigation item with delete icon.
- Filter `DiscardedNote` into Trash.
- Hide homepage add note action in Trash.
- Add Trash empty state title `No notes in the trash`.
- Make discarded note cards route-ready for the detail screen.
- Add home filtering tests for Trash.
- Add add-note action visibility tests for Trash.
- Verify Trash navigation manually.

## 22. Add Trash Detail And Actions (Done)

- Add `DiscardedNote.restore(restoredAt)` transition to active note with no reminder.
- Add `RestoreDiscardedNote` feature.
- Rename `DeleteNote` to `PermanentlyDeleteNote`.
- Ensure `PermanentlyDeleteNote` only succeeds for discarded notes.
- Add `DiscardedNoteDetailsScreen` as a duplicated read-only screen for now.
- Show discarded note title and description read-only.
- Do not show discarded date.
- Add back action.
- Add restore action.
- Add permanently delete action with delete forever icon.
- Restore discarded note and open restored active note in editor.
- Permanently delete discarded note and return home.
- Add tests for discarded note restore transition.
- Add tests for restoring discarded notes.
- Add tests for permanently deleting only discarded notes.
- Verify Trash details manually.

## 23. Refactor Common Read-Only Details Component (Done)

- Extract common read-only note details UI from archived and discarded details screens.
- Keep archived and discarded route wiring separate.
- Preserve archived details behavior.
- Preserve discarded details behavior.
- Add preview coverage if useful.
- Verify both read-only details screens still work.

## 24. Add Saved URL Links In Notes (Done)

- Detect `http://` and `https://` URLs in saved note descriptions.
- Do not detect bare domains.
- Style detected URLs with underline and current theme `onSurface` color.
- Keep newly typed or pasted URLs plain until note is reopened.
- Initialize reopened editor descriptions with saved URL annotations.
- Single tap saved URL in editor opens default external browser.
- Single tap non-URL text keeps normal cursor movement behavior.
- Show clickable URLs in read-only archived and discarded note details.
- Keep note persistence as plain text.
- Add URL detection tests if practical.
- Verify autosave still persists plain description text.
- Verify external browser opens for saved URLs.

## 25. Auto-Delete Old Discarded Notes (Done)

- Add repository support to delete discarded notes before a cutoff instant.
- Add DAO query to delete `DISCARDED` notes by `discardedAtMillis` cutoff.
- Add `DeleteExpiredDiscardedNotes` feature.
- Use exact 30-day retention based on `discardedAt`.
- Run cleanup once on app open from `MainActivity.onCreate`.
- Run cleanup on `Dispatchers.IO` without blocking UI startup.
- Wire feature in `NotedAppContainer`.
- Add tests for deleting discarded notes at or older than 30 days.
- Add tests for keeping discarded notes newer than 30 days.
- Add tests for keeping active and archived notes.
- Add repository tests for cutoff delete.
- Verify app-open cleanup does not run on destination changes.

## 26. Add Persisted Notes Grid Layout (Done)

- Add notes layout mode with list and grid values.
- Persist selected notes layout with `SharedPreferences`.
- Wire layout preference through `NotedAppContainer`.
- Add layout to `HomeUiState`.
- Load persisted layout in `HomeViewModel`.
- Add layout toggle action in `HomeViewModel`.
- Add top app bar on Notes, Archive, and Trash.
- Show destination title in the top app bar.
- Add top app bar layout toggle icon.
- Keep one layout preference shared by all home destinations.
- Keep existing list layout with `LazyColumn`.
- Add two-column grid layout with `LazyVerticalStaggeredGrid`.
- Reuse existing note cards in list and grid.
- Make note cards accept external modifiers.
- Add tests for persisted layout loading and toggling.
- Verify tests and lint pass.

## 27. Add Backup JSON Format (Done)

- Add Kotlin serialization Gradle plugin.
- Add `kotlinx-serialization-json` dependency.
- Add backup DTOs for schema version 1.
- Use ISO-8601 strings for instants.
- Use UUID strings for note IDs.
- Encode all note lifecycle states to backup DTOs.
- Decode schema version 1 backup DTOs to domain notes.
- Reject unsupported schema versions with a domain error.
- Reject malformed backup JSON with a domain error.
- Reject invalid lifecycle field combinations with a domain error.
- Keep duplicate note IDs in a file by letting the last note survive.
- Add codec tests for active notes.
- Add codec tests for archived notes.
- Add codec tests for discarded notes.
- Add codec tests for unsupported version errors.
- Add codec tests for malformed file errors.
- Add codec tests for duplicate IDs keeping last.
- Verify tests pass.

## 28. Export Notes (Done)

- Add repository `loadAll` support.
- Implement `loadAll` in `NoteDao`.
- Implement `loadAll` in `RoomNoteRepository`.
- Add repository tests for `loadAll`.
- Add `ExportNotes` feature returning JSON text.
- Include `schemaVersion = 1`.
- Include `exportedAt` from `Clock`.
- Include all notes, not only active notes.
- Use default filename `noted-backup-yyyy-MM-dd.json`.
- Wire `ExportNotes` in `NotedAppContainer`.
- Add feature tests for exporting all lifecycle states.
- Add home top-bar overflow menu.
- Add Export action.
- Use `CreateDocument("application/json")`.
- Write exported JSON to selected URI.
- Show export success message.
- Show export failure message.
- Verify export manually with device file picker.

## 29. Import Notes (Done)

- Add repository bulk upsert support.
- Make Room bulk upsert transactional.
- Add tests for transactional bulk upsert rollback.
- Add `ImportNotes` feature that parses and validates all notes before writing.
- Append notes with new UUIDs.
- Override existing DB notes with same UUID.
- Preserve imported timestamps.
- Cancel alarms for every imported note ID after successful import.
- Schedule only active imported notes with future reminders after successful import.
- Do not schedule past reminders.
- Do not schedule archived or discarded notes.
- Wire `ImportNotes` in `NotedAppContainer`.
- Add tests for append import.
- Add tests for same UUID override.
- Add tests for transaction rollback when any imported note is invalid.
- Add tests for canceling imported note alarms.
- Add tests for scheduling active future reminders.
- Add tests for skipping past, archived, and discarded reminders.
- Verify tests pass.

## 30. Add Import UI And Permission Gate (Done)

- Reuse or extract reminder permission logic from editor.
- Add tests for import permission requirement decision.
- Add Import action to home top-bar overflow menu.
- Use `OpenDocument` for JSON files.
- Read selected URI text.
- Validate import before requesting permissions.
- If import has no active future reminders, import without permission prompts.
- If import has active future reminders, require notification permission before import.
- If import has active future reminders, require exact alarm permission before import.
- Block import until required permissions are granted.
- Surface permission denial or settings redirect without importing.
- Run transactional import after permissions pass.
- Show imported note count.
- Show unsupported version error clearly.
- Show malformed file error clearly.
- Verify import manually with exported file.

## 31. Document Import/Export

- Update `ARCHITECTURE.md` with import/export features and files.
- Update architecture docs if new package/file roles need mention.
- Run full unit tests.
- Run lint.
- Run app build.
- Verify export then import round trip manually.
