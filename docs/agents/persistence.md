# Persistence

- Room uses a flat `NoteEntity`; `RoomNoteRepository` maps entities to domain objects.
- `NotedDatabase` has `exportSchema = true`; Room schemas are checked in under `app/schemas/` and
  must be updated when schema changes.
