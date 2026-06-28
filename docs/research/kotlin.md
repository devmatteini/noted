# Kotlin Notes For Noted

These notes cover Kotlin basics useful for implementing Noted.

## Variables

Use `val` by default. It is immutable.

```kotlin
val appName = "Noted"
```

Use `var` only when the value must change.

```kotlin
var selectedTagCount = 0
```

## Functions

Basic function:

```kotlin
fun isEmptyDescription(value: String): Boolean {
    return value.trim().isEmpty()
}
```

Short expression function:

```kotlin
fun isEmptyDescription(value: String): Boolean = value.trim().isEmpty()
```

## Data Classes

Use data classes for immutable domain models.

```kotlin
data class ActiveNote(
    val id: NoteId,
    val title: NoteTitle?,
    val description: NoteDescription,
    val reminder: Reminder?,
    val tags: List<Tag>,
)
```

Update by copying, not mutating.

```kotlin
val updated = note.copy(description = newDescription)
```

## Null Safety

Nullable values use `?`.

```kotlin
val title: NoteTitle? = null
```

Safe access:

```kotlin
val titleText = note.title?.value
```

Fallback with Elvis operator:

```kotlin
val displayTitle = note.title?.value ?: "Untitled"
```

Avoid `!!`. It crashes if the value is null.

```kotlin
val unsafe = note.title!!.value
```

## Sealed Interfaces

Use sealed interfaces for closed sum types.

In Noted:

```text
Note = ActiveNote | ArchivedNote
```

Kotlin version:

```kotlin
sealed interface Note {
    val id: NoteId
    val title: NoteTitle?
    val description: NoteDescription
    val tags: List<Tag>
}
```

`ActiveNote` and `ArchivedNote` override the shared fields because they promise to implement the
`Note` contract.

```kotlin
data class ActiveNote(
    override val id: NoteId,
    override val title: NoteTitle?,
    override val description: NoteDescription,
    val reminder: Reminder?,
    override val tags: List<Tag>,
) : Note
```

```kotlin
data class ArchivedNote(
    override val id: NoteId,
    override val title: NoteTitle?,
    override val description: NoteDescription,
    override val tags: List<Tag>,
    val archivedAt: Instant,
) : Note
```

This lets code use common fields without knowing the exact note type.

```kotlin
fun displayTitle(note: Note): String =
    note.title?.value ?: note.description.value.take(30)
```

Variant-specific fields still require checking the variant.

```kotlin
fun reminderOrNull(note: Note): Reminder? =
    when (note) {
        is ActiveNote -> note.reminder
        is ArchivedNote -> null
    }
```

Important Noted rule: `ArchivedNote` has no `reminder` field, so the type system prevents archived
reminders.

## Value Classes

Use value classes for meaningful wrappers around primitive values.

```kotlin
@JvmInline
value class NoteId(val value: UUID)

@JvmInline
value class TagId(val value: UUID)
```

This prevents passing a `TagId` where a `NoteId` is expected.

```kotlin
fun deleteNote(id: NoteId)
```

Useful Noted value classes:

```kotlin
@JvmInline value class NoteId(val value: UUID)
@JvmInline value class TagId(val value: UUID)
@JvmInline value class NoteTitle(val value: String)
@JvmInline value class NoteDescription(val value: String)
@JvmInline value class TagName(val value: String)
```

## Smart Constructors

Use smart constructors when values need validation.

`NoteDescription` must be non-empty after trimming.

```kotlin
@JvmInline
value class NoteDescription private constructor(val value: String) {
    companion object {
        fun create(value: String): NoteDescription? {
            val trimmed = value.trim()
            return if (trimmed.isEmpty()) null else NoteDescription(trimmed)
        }
    }
}
```

Usage:

```kotlin
val description = NoteDescription.create(input)

if (description == null) {
    // Show validation error.
    return
}
```

## `when`

`when` is Kotlin's switch-like pattern matching construct.

With sealed types, it is exhaustive.

```kotlin
fun noteStatusText(note: Note): String =
    when (note) {
        is ActiveNote -> "Active"
        is ArchivedNote -> "Archived"
    }
```

If `Note` has only `ActiveNote` and `ArchivedNote`, Kotlin knows all cases are handled.

For strings, use `else` because Kotlin cannot know all possible strings.

```kotlin
fun entityStatusText(status: String): String =
    when (status) {
        "ACTIVE" -> "Active"
        "ARCHIVED" -> "Archived"
        else -> error("Unknown note status: $status")
    }
```

Enums can also be exhaustive.

```kotlin
enum class NoteStatus {
    ACTIVE,
    ARCHIVED,
}

fun statusText(status: NoteStatus): String =
    when (status) {
        NoteStatus.ACTIVE -> "Active"
        NoteStatus.ARCHIVED -> "Archived"
    }
```

## Coroutines And `suspend`

`suspend fun` means the function can suspend inside a coroutine without blocking the thread.

```kotlin
suspend fun deleteNote(id: NoteId) {
    repository.delete(id)
}
```

Calling a suspend function from another suspend function looks normal.

```kotlin
suspend fun archiveNote(id: NoteId) {
    reminderScheduler.cancel(id)
    repository.archive(id)
}
```

This is sequential:

```text
cancel finishes
then archive runs
```

You do not write `await` for normal suspend calls.

You can only call a suspend function from another suspend function or from a coroutine.

Example from a ViewModel:

```kotlin
viewModelScope.launch {
    deleteNote(noteId)
}
```

If you explicitly start concurrent work with `async`, then you use `await`.

```kotlin
coroutineScope {
    val note = async { repository.getNote(id) }
    val tags = async { repository.getTags(id) }

    note.await() to tags.await()
}
```

Rule:

```text
normal suspend call = waits automatically
async { ... } = returns Deferred, needs await()
```

## Flow

`Flow` is an async stream.

Room can expose changing database data as `Flow`.

```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun observeNotes(): Flow<List<NoteEntity>>
}
```

Repository maps Room entities to domain models.

```kotlin
class RoomNoteRepository(
    private val noteDao: NoteDao,
) : NoteRepository {
    override fun observeNotes(): Flow<List<Note>> =
        noteDao.observeNotes().map { entities ->
            entities.map { it.toDomain() }
        }
}
```

## Feature Classes

Feature classes represent app actions.

```kotlin
class DeleteNote(
    private val repository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend operator fun invoke(id: NoteId) {
        reminderScheduler.cancel(id)
        repository.delete(id)
    }
}
```

`operator fun invoke` lets the object be called like a function.

```kotlin
deleteNote(noteId)
```

## Exceptions And `error()`

Kotlin commonly uses exceptions.

`error("message")` throws an `IllegalStateException`.

```kotlin
val description = NoteDescription.create(entity.description)
    ?: error("Invalid persisted note description")
```

Use this for impossible or corrupted state.

For example, if the database contains an empty note description, that should never happen.

Do not use exceptions for normal user input validation.

Rule:

```text
bad user input = value/result/null
impossible or corrupt state = exception/error()
```

## Error Handling With `Result`

Kotlin has `Result<T>` for success/failure values.

`Result.failure(...)` stores a `Throwable`.

```kotlin
@JvmInline
value class NoteDescription private constructor(val value: String) {
    companion object {
        fun create(value: String): Result<NoteDescription> {
            val trimmed = value.trim()

            return if (trimmed.isEmpty()) {
                Result.failure(IllegalArgumentException("Description cannot be empty"))
            } else {
                Result.success(NoteDescription(trimmed))
            }
        }
    }
}
```

Usage:

```kotlin
val result = NoteDescription.create(input)

result
    .onSuccess { description ->
        // Save note.
    }
    .onFailure { error ->
        // Show error.message to user.
    }
```

Manual unwrap:

```kotlin
val description = NoteDescription.create(input).getOrElse { error ->
    println(error.message)
    return
}
```

Feature example:

```kotlin
class CreateNote(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(descriptionInput: String): Result<ActiveNote> {
        val description = NoteDescription.create(descriptionInput)
            .getOrElse { return Result.failure(it) }

        val note = ActiveNote(
            id = NoteId(UUID.randomUUID()),
            title = null,
            description = description,
            reminder = null,
            tags = emptyList(),
        )

        repository.save(note)

        return Result.success(note)
    }
}
```

ViewModel usage:

```kotlin
viewModelScope.launch {
    createNote(descriptionInput)
        .onSuccess {
            // Navigate back.
        }
        .onFailure { error ->
            // Update UI error state.
            errorMessage = error.message ?: "Could not create note"
        }
}
```

## Error Handling With Sealed Results

For expected domain or UI errors, sealed result types are often clearer than `Result<T>`.

```kotlin
sealed interface CreateNoteResult {
    data class Success(val note: ActiveNote) : CreateNoteResult
    data object EmptyDescription : CreateNoteResult
}
```

Feature:

```kotlin
class CreateNote(
    private val repository: NoteRepository,
) {
    suspend operator fun invoke(descriptionInput: String): CreateNoteResult {
        val description = NoteDescription.create(descriptionInput)
            ?: return CreateNoteResult.EmptyDescription

        val note = ActiveNote(
            id = NoteId(UUID.randomUUID()),
            title = null,
            description = description,
            reminder = null,
            tags = emptyList(),
        )

        repository.save(note)

        return CreateNoteResult.Success(note)
    }
}
```

Usage:

```kotlin
when (val result = createNote(descriptionInput)) {
    is CreateNoteResult.Success -> {
        // Navigate back.
    }

    CreateNoteResult.EmptyDescription -> {
        // Show "Description cannot be empty".
    }
}
```

Recommended rule for Noted:

```text
Result<T> = useful for IO/infrastructure failures
sealed result = better for expected validation/domain errors
```

## Room Entity To Domain Mapping

Room can store a flatter model than the domain.

```kotlin
data class NoteEntity(
    val id: String,
    val title: String?,
    val description: String,
    val reminderAtMillis: Long?,
    val status: String,
    val archivedAtMillis: Long?,
)
```

Map to safer domain types in the repository/infrastructure layer.

```kotlin
fun NoteEntity.toDomain(): Note {
    val id = NoteId(UUID.fromString(id))
    val title = title?.let { NoteTitle(it) }
    val description = NoteDescription.create(description)
        ?: error("Invalid persisted note description")

    return when (status) {
        "ACTIVE" -> ActiveNote(
            id = id,
            title = title,
            description = description,
            reminder = reminderAtMillis?.let { Reminder.fromMillis(it) },
            tags = emptyList(),
        )

        "ARCHIVED" -> ArchivedNote(
            id = id,
            title = title,
            description = description,
            tags = emptyList(),
            archivedAt = Instant.ofEpochMilli(
                archivedAtMillis ?: error("Missing archivedAt"),
            ),
        )

        else -> error("Unknown note status: $status")
    }
}
```

## Manual Dependency Injection

Noted starts without Hilt. Use a simple composition container.

```kotlin
class NotedAppContainer(context: Context) {
    private val database = Room.databaseBuilder(
        context,
        NotedDatabase::class.java,
        "noted.db",
    ).build()

    private val repository = RoomNoteRepository(
        noteDao = database.noteDao(),
    )

    private val reminderScheduler = AlarmReminderScheduler(context)

    val createNote = CreateNote(repository)
    val deleteNote = DeleteNote(repository, reminderScheduler)
    val observeNotes = ObserveNotes(repository)
}
```

## Things To Learn First

1. `val`, `var`, functions, classes, data classes.
2. Null safety: `?`, `?.`, `?:`, avoid `!!`.
3. Sealed interfaces and exhaustive `when`.
4. Value classes and smart constructors.
5. Coroutines: `suspend`, `launch`, `async`, `await`.
6. `Flow` and `StateFlow`.
7. Jetpack Compose state and callbacks.
8. Room entities, DAOs, and mapping.
9. Android alarms, notifications, and permissions.
