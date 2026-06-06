# Architecture Rules

- Source packages are intentionally flat under `domain/`, `features/`, `infrastructure/`, and `ui/`.
- Dependency direction: `ui -> features -> domain`; `infrastructure -> domain`.
- Do not import Android or infrastructure from `domain/` or `features/`.
- Feature classes live in `features/` and are named as app actions, not `usecase/`.
- Domain notes are a sealed model: `ActiveNote` and `ArchivedNote`; archived domain notes must not
  expose reminders.
- Domain value types use Kotlin value classes where meaningful, for example `NoteId`, `NoteTitle`,
  `NoteDescription`.
