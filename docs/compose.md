# Jetpack Compose Notes For Noted

Compose is Android's modern declarative UI toolkit. UI is written as Kotlin functions instead of XML.

## Mental Model

Compose UI is a function of state.

```text
state -> UI
events -> ViewModel/features -> new state -> UI
```

For Noted:

```text
Room Flow -> ViewModel StateFlow -> Compose screen
user click -> ViewModel -> feature class -> repository -> Room
```

Do not manually mutate views. Change state, and Compose redraws the affected UI.

## Composable Functions

A composable is a Kotlin function annotated with `@Composable`.

```kotlin
@Composable
fun HomeScreen() {
    Text("Noted")
}
```

Composable functions can call other composable functions.

```kotlin
@Composable
fun HomeScreen(state: HomeUiState) {
    Column {
        AppHeader()
        NoteList(notes = state.notes)
    }
}
```

Keep composables mostly UI. Avoid database, repository, alarm scheduling, or domain transitions inside them.

Good:

```kotlin
@Composable
fun NoteCard(
    note: NoteListItem,
    onClick: () -> Unit,
) {
    Card(onClick = onClick) {
        Text(note.title)
        Text(note.description)
    }
}
```

Bad:

```kotlin
@Composable
fun NoteCard(note: Note) {
    val database = Room.databaseBuilder(...)
}
```

## Basic Layouts

`Column` places children vertically.

```kotlin
Column {
    Text("Title")
    Text("Description")
}
```

`Row` places children horizontally.

```kotlin
Row {
    Text("Tag")
    Text("Archive")
}
```

`Box` overlays children.

```kotlin
Box {
    Text("Note")
    FloatingActionButton(onClick = onAddNote) {
        Text("+")
    }
}
```

`Scaffold` gives common Material screen slots.

```kotlin
Scaffold(
    floatingActionButton = {
        FloatingActionButton(onClick = onAddNote) {
            Text("+")
        }
    },
) { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
    ) {
        Text("Noted")
    }
}
```

## Modifiers

`Modifier` changes layout, size, padding, click behavior, background, etc.

```kotlin
Text(
    text = "Noted",
    modifier = Modifier.padding(16.dp),
)
```

Common modifiers:

```kotlin
Modifier.fillMaxSize()
Modifier.fillMaxWidth()
Modifier.padding(16.dp)
Modifier.clickable { onClick() }
Modifier.weight(1f)
```

Order matters.

```kotlin
Modifier
    .padding(16.dp)
    .background(Color.Red)
```

This differs from:

```kotlin
Modifier
    .background(Color.Red)
    .padding(16.dp)
```

First: padding outside red background. Second: padding inside red background.

## State

Compose redraws when state changes.

Local state:

```kotlin
var title by remember { mutableStateOf("") }

TextField(
    value = title,
    onValueChange = { title = it },
)
```

`remember` keeps state across recompositions.

`rememberSaveable` survives configuration changes better, when possible.

```kotlin
var description by rememberSaveable { mutableStateOf("") }
```

`remember` only survives recomposition.

```kotlin
var title by remember { mutableStateOf("") }
```

If the composable redraws, `title` stays.

But if Android recreates the Activity, `remember` state can be lost. Common causes:

- screen rotation
- theme/dark mode change
- language change
- process recreation after app is backgrounded

`rememberSaveable` stores simple state in Android saved instance state.

```kotlin
var title by rememberSaveable { mutableStateOf("") }
```

If the user typed `Buy milk` and rotates the phone, the field is more likely to still contain `Buy milk`.

Rule:

```text
remember = keep during recomposition
rememberSaveable = keep during recomposition + many Activity recreations
ViewModel = better for screen/app state
database = permanent
```

For Noted editor, prefer ViewModel for title/description if the editor coordinates validation, reminders, permissions, and loading existing notes.

Use local state for temporary UI-only values.

Use ViewModel state for app data, filters, editor data, permissions flow, and save behavior.

## State Hoisting

State hoisting means parent owns state, child receives value and callback.

```kotlin
@Composable
fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        label = { Text("Description") },
    )
}
```

Prefer screen APIs like this:

```kotlin
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
)
```

This keeps composables reusable and testable.

## ViewModel And Compose

The ViewModel owns screen state and calls feature classes.

```kotlin
data class HomeUiState(
    val notes: List<NoteListItem> = emptyList(),
    val showArchived: Boolean = false,
)
```

```kotlin
class HomeViewModel(
    observeNotes: ObserveNotes,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        observeNotes()
            .map { notes ->
                HomeUiState(notes = notes.map { it.toListItem() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )
}
```

Compose collects state with lifecycle awareness.

```kotlin
@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onAddNote = viewModel::onAddNote,
        onOpenNote = viewModel::onOpenNote,
        onToggleArchived = viewModel::onToggleArchived,
    )
}
```

Prefer route/screen split:

```text
HomeRoute = connects ViewModel to UI
HomeScreen = stateless UI
```

## Events

UI emits events. ViewModel decides what they mean.

```kotlin
Button(onClick = onSave) {
    Text("Save")
}
```

ViewModel:

```kotlin
fun onSave() {
    viewModelScope.launch {
        createNote(descriptionInput)
    }
}
```

Avoid calling feature classes directly from composables.

## Lists

Use `LazyColumn` for note lists.

```kotlin
@Composable
fun NoteList(
    notes: List<NoteListItem>,
    onOpenNote: (NoteId) -> Unit,
) {
    LazyColumn {
        items(
            items = notes,
            key = { it.id.value.toString() },
        ) { note ->
            NoteCard(
                note = note,
                onClick = { onOpenNote(note.id) },
            )
        }
    }
}
```

Always provide stable keys for database-backed lists. For Noted, use note UUID.

## Text Fields

The editor screen needs controlled text fields.

```kotlin
@Composable
fun NoteEditorScreen(
    title: String,
    description: String,
    descriptionError: String?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column {
        TextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title") },
            singleLine = true,
        )

        TextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            isError = descriptionError != null,
            supportingText = {
                if (descriptionError != null) {
                    Text(descriptionError)
                }
            },
            minLines = 8,
        )

        Button(
            onClick = onSave,
            enabled = description.isNotBlank(),
        ) {
            Text("Save")
        }
    }
}
```

For Noted, description is mandatory. UI can disable save while blank, show an error after save attempt, or both. Feature/domain validation must still exist.

## One-Time Effects

Some actions are not normal state rendering:

- navigate after save
- show snackbar
- open exact alarm settings
- request notification permission

Use one-time events from ViewModel, collected in `LaunchedEffect`.

```kotlin
sealed interface NoteEditorEvent {
    data object NavigateBack : NoteEditorEvent
    data class ShowMessage(val message: String) : NoteEditorEvent
    data object RequestNotificationPermission : NoteEditorEvent
    data object OpenExactAlarmSettings : NoteEditorEvent
}
```

ViewModel exposes events:

```kotlin
private val _events = Channel<NoteEditorEvent>()
val events = _events.receiveAsFlow()
```

Compose collects events:

```kotlin
@Composable
fun NoteEditorRoute(viewModel: NoteEditorViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NoteEditorEvent.NavigateBack -> {
                    // navController.popBackStack()
                }

                is NoteEditorEvent.ShowMessage -> {
                    // snackbarHostState.showSnackbar(event.message)
                }

                NoteEditorEvent.RequestNotificationPermission -> {
                    // permissionLauncher.launch(...)
                }

                NoteEditorEvent.OpenExactAlarmSettings -> {
                    // context.startActivity(...)
                }
            }
        }
    }

    NoteEditorScreen(
        state = state,
        onSave = viewModel::onSave,
    )
}
```

Do not put navigation or snackbar calls directly in the normal composable body. Recomposition can run the body many times.

## Side Effects

Use Compose side-effect APIs intentionally.

Use `LaunchedEffect` for coroutine work tied to composition.

```kotlin
LaunchedEffect(noteId) {
    viewModel.load(noteId)
}
```

Use `rememberLauncherForActivityResult` for permission requests.

```kotlin
val launcher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
) { granted ->
    viewModel.onNotificationPermissionResult(granted)
}
```

Use `rememberUpdatedState` when an effect needs the latest lambda without restarting.

```kotlin
val latestOnTimeout by rememberUpdatedState(onTimeout)

LaunchedEffect(Unit) {
    delay(1_000)
    latestOnTimeout()
}
```

For this app, likely side effects:

- notification permission request
- exact alarm settings intent
- snackbar message
- navigation after save/delete
- date/time picker result

## Reminder Permission UX

Architecture rule:

```text
Do not ask permissions on app start.
Ask only when user sets a reminder.
```

UI flow:

```text
User taps reminder picker
-> user chooses date/time
-> ViewModel checks if permissions needed
-> UI requests notification permission if needed
-> ViewModel receives result
-> UI opens exact alarm settings if needed
-> ViewModel saves reminder only if both granted
```

Compose should handle launchers and intents. ViewModel should coordinate state.

```kotlin
fun onReminderSelected(dateTime: LocalDateTime) {
    pendingReminder = dateTime
    viewModelScope.launch {
        if (!permissions.hasNotificationPermission()) {
            _events.send(NoteEditorEvent.RequestNotificationPermission)
            return@launch
        }

        if (!permissions.canScheduleExactAlarms()) {
            _events.send(NoteEditorEvent.OpenExactAlarmSettings)
            return@launch
        }

        saveReminder(dateTime)
    }
}
```

`return@launch` means return from the `launch` block, not from the outer function.

```kotlin
fun onReminderSelected(dateTime: LocalDateTime) {
    viewModelScope.launch {
        if (!permissions.hasNotificationPermission()) {
            _events.send(NoteEditorEvent.RequestNotificationPermission)
            return@launch
        }

        saveReminder(dateTime)
    }
}
```

Without permission, this sends the event and stops the coroutine block. `saveReminder(dateTime)` does not run.

This is needed because `launch { ... }` takes a lambda. Labeled returns say which lambda/function to exit.

```text
return@launch = exit the launch lambda
```

Equivalent without early return:

```kotlin
viewModelScope.launch {
    if (!permissions.hasNotificationPermission()) {
        _events.send(NoteEditorEvent.RequestNotificationPermission)
    } else {
        saveReminder(dateTime)
    }
}
```

The exact Android permission implementation belongs in `ui/` or `infrastructure/`, not `domain/`.

## Recomposition

Recomposition means Compose reruns composable functions when state changes.

Rules:

- composables can run many times
- composables should be fast
- avoid side effects in composable body
- do not create databases/repositories in composables
- do not launch random coroutines directly in composable body
- keep expensive work in ViewModel or use derived state carefully

Bad:

```kotlin
@Composable
fun HomeScreen(notes: List<Note>) {
    repository.deleteOldNotes()
    Text("Noted")
}
```

Good:

```kotlin
@Composable
fun HomeScreen(notes: List<Note>) {
    Text("Noted")
}
```

## `remember`

`remember` keeps a value across recompositions.

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
```

Use it for UI objects and local state.

```kotlin
var showArchived by rememberSaveable { mutableStateOf(false) }
```

If state affects app behavior or must survive screen recreation, prefer ViewModel.

For Noted:

```text
editor text before save = ViewModel or rememberSaveable
note list = ViewModel
filters = ViewModel if they affect query/list behavior
snackbar host = remember
permission launcher = rememberLauncherForActivityResult
```

## Derived State

Sometimes state is derived from other state.

```kotlin
val canSave = description.isNotBlank()
```

Usually simple local values are enough.

Use `derivedStateOf` only when the derived value is expensive or changes much less often than inputs.

```kotlin
val canSave by remember(description) {
    derivedStateOf { description.isNotBlank() }
}
```

Do not overuse it.

## Stability

Compose performs better when parameters are stable and immutable.

Good UI state:

```kotlin
data class HomeUiState(
    val notes: List<NoteListItem> = emptyList(),
    val showArchived: Boolean = false,
)
```

Good list item:

```kotlin
data class NoteListItem(
    val id: NoteId,
    val title: String,
    val descriptionPreview: String,
    val reminderText: String?,
    val tagNames: List<String>,
    val archived: Boolean,
)
```

Avoid passing huge domain objects everywhere if the UI only needs display fields. Mapping to UI models keeps screens simple.

## Previews

Previews render composables without running the full app.

```kotlin
@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        state = HomeUiState(
            notes = listOf(
                NoteListItem(
                    id = NoteId(UUID.randomUUID()),
                    title = "Grocery list",
                    descriptionPreview = "Milk, eggs, coffee",
                    reminderText = "Today 18:00",
                    tagNames = listOf("Home"),
                    archived = false,
                ),
            ),
        ),
        onAddNote = {},
        onOpenNote = {},
        onToggleArchived = {},
    )
}
```

Previews are easier when `HomeScreen` does not require a ViewModel.

## Navigation

For MVP, navigation can stay simple.

```text
Home
Editor create
Editor edit(noteId)
```

Route examples:

```kotlin
object Routes {
    const val HOME = "home"
    const val CREATE_NOTE = "note/new"
    const val EDIT_NOTE = "note/{noteId}"
}
```

Basic nav host:

```kotlin
NavHost(
    navController = navController,
    startDestination = Routes.HOME,
) {
    composable(Routes.HOME) {
        HomeRoute(...)
    }

    composable(Routes.CREATE_NOTE) {
        NoteEditorRoute(...)
    }

    composable(Routes.EDIT_NOTE) { entry ->
        val noteId = entry.arguments?.getString("noteId")
        NoteEditorRoute(...)
    }
}
```

This app has two screens. Do not overbuild navigation.

## Recommended Screen Shape

Use this split:

```text
HomeRoute.kt
HomeScreen.kt
HomeViewModel.kt
NoteEditorRoute.kt
NoteEditorScreen.kt
NoteEditorViewModel.kt
```

The route/screen split can live in the same file at first.

```kotlin
@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onAddNote = viewModel::onAddNote,
        onOpenNote = viewModel::onOpenNote,
        onToggleArchived = viewModel::onToggleArchived,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onAddNote: () -> Unit,
    onOpenNote: (NoteId) -> Unit,
    onToggleArchived: () -> Unit,
) {
    // Stateless UI.
}
```

## Homepage Shape

Architecture requires:

- app title
- settings entry
- tag filter row
- archived filter
- note list
- add note action

Possible structure:

```kotlin
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSettingsClick: () -> Unit,
    onTagClick: (TagId) -> Unit,
    onArchivedToggle: () -> Unit,
    onNoteClick: (NoteId) -> Unit,
    onAddNote: () -> Unit,
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Text("+")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            HomeTopBar(onSettingsClick = onSettingsClick)

            TagFilterRow(
                tags = state.tags,
                selectedTagIds = state.selectedTagIds,
                onTagClick = onTagClick,
            )

            ArchivedFilter(
                showArchived = state.showArchived,
                onToggle = onArchivedToggle,
            )

            NoteList(
                notes = state.notes,
                onNoteClick = onNoteClick,
            )
        }
    }
}
```

## Editor Shape

Architecture requires:

- full-screen note editor
- optional title field
- mandatory description field
- optional reminder picker
- optional tags
- save action disabled or rejected if description is empty

Possible structure:

```kotlin
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onReminderClick: () -> Unit,
    onRemoveReminder: () -> Unit,
    onTagClick: (TagId) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit note" else "New note") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onSave,
                        enabled = state.description.isNotBlank(),
                    ) {
                        Text("Save")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            TextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = state.description,
                onValueChange = onDescriptionChange,
                label = { Text("Description") },
                isError = state.descriptionError != null,
                supportingText = {
                    state.descriptionError?.let { Text(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            ReminderRow(
                reminderText = state.reminderText,
                onReminderClick = onReminderClick,
                onRemoveReminder = onRemoveReminder,
            )

            TagPickerRow(
                tags = state.tags,
                selectedTagIds = state.selectedTagIds,
                onTagClick = onTagClick,
            )
        }
    }
}
```

## Best Practices For Noted

Keep composables stateless when possible.

Use stable list keys.

```kotlin
items(notes, key = { it.id.value.toString() }) { note -> ... }
```

Use `collectAsStateWithLifecycle()` for `StateFlow`.

```kotlin
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

Use ViewModel for note editor state if it must coordinate save, permissions, reminders, loading existing note.

Use `LaunchedEffect` for one-time event collection.

Do not call repositories, Room, or feature classes directly from composables.

Do not request notification/exact alarm permissions on app start.

Do not store `Context` in composable state.

Prefer UI-specific models over raw domain objects for list rows.

Keep the UI simple. This app has two screens; avoid heavy abstractions.

## Common Mistakes

Putting side effects in composable body:

```kotlin
@Composable
fun Screen() {
    viewModel.load()
}
```

Use:

```kotlin
LaunchedEffect(Unit) {
    viewModel.load()
}
```

Mutating lists:

```kotlin
state.notes.add(note)
```

Use immutable lists:

```kotlin
state.copy(notes = state.notes + note)
```

Forgetting stable lazy list keys:

```kotlin
items(notes) { note -> ... }
```

Prefer:

```kotlin
items(notes, key = { it.id.value.toString() }) { note -> ... }
```

Doing validation only in UI:

```kotlin
enabled = description.isNotBlank()
```

This is fine for UX, but feature/domain code must still validate.

## What To Learn First

1. `@Composable`, `Text`, `Button`, `TextField`.
2. `Column`, `Row`, `Box`, `Scaffold`.
3. `Modifier`.
4. `remember` and `rememberSaveable`.
5. State hoisting: `value` + `onValueChange`.
6. ViewModel + `StateFlow`.
7. `collectAsStateWithLifecycle`.
8. `LazyColumn` with stable keys.
9. `LaunchedEffect` and one-time events.
10. Permission launchers and activity results.
11. Navigation Compose.

## Noted UI Boundary

```text
Composable = render state + emit callbacks
ViewModel = hold UI state + call features
features = app actions
infrastructure = Room/AlarmManager/notifications
```
