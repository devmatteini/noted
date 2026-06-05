# Android Notes For Noted

These notes cover Android basics and app-specific practices useful for implementing Noted.

## Mental Model

Android apps are component-based. The system can start, stop, pause, recreate, or kill parts of your
app.

For Noted, main Android responsibilities are:

- host Compose UI in an Activity
- persist notes with Room
- request notification permission only when needed
- check exact alarm access only when needed
- schedule exact alarms with `AlarmManager`
- receive alarm broadcasts with `BroadcastReceiver`
- show reminder notifications
- restore alarms after device reboot

Keep this boundary:

```text
domain = pure Kotlin business model
features = app actions
ui = Compose, ViewModel, permissions UX, navigation
infrastructure = Room, AlarmManager, BroadcastReceiver, notifications
```

## App Components

Important Android components for Noted:

- `Activity`: hosts the UI.
- `ViewModel`: stores screen state and runs UI-related coroutines.
- `RoomDatabase`: SQLite database wrapper.
- `BroadcastReceiver`: receives alarm and reboot broadcasts.
- `AlarmManager`: schedules exact reminders.
- `NotificationManager`: shows reminder notifications.
- `Application`: optional app-wide setup and dependency container owner.

Noted likely needs one main Activity.

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NotedApp()
        }
    }
}
```

## Lifecycle Basics

Android lifecycle matters because the system controls app components.

An Activity can be:

```text
created -> started -> resumed -> paused -> stopped -> destroyed
```

Common reasons Activity is recreated:

- rotation
- dark mode/theme change
- language change
- process recreation after backgrounding

Implications:

- do not store important data only in Activity fields
- use ViewModel for screen state
- use Room for persistent note data
- use `rememberSaveable` only for simple UI values
- do not assume the app process lives forever

## Activity

For a Compose-only app, the Activity should be thin.

Good Activity responsibilities:

- call `setContent`
- provide app container/ViewModels
- setup app theme

Avoid:

- database queries directly in Activity
- note validation in Activity
- alarm scheduling logic in Activity

Example shape:

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as NotedApplication).container

        setContent {
            NotedApp(appContainer = appContainer)
        }
    }
}
```

## Application

`Application` lives as long as the app process. It is a practical place to create the manual
dependency container.

```kotlin
class NotedApplication : Application() {
    lateinit var container: NotedAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = NotedAppContainer(this)
    }
}
```

Register it in `AndroidManifest.xml`:

```xml

<application android:name=".NotedApplication"...></application>
```

## Manifest

The manifest declares app components and permissions.

Noted likely needs:

```xml

<uses-permission android:name="android.permission.POST_NOTIFICATIONS" /><uses-permission
android:name="android.permission.SCHEDULE_EXACT_ALARM" /><uses-permission
android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Receivers:

```xml

<receiver android:name=".infrastructure.ReminderReceiver" android:exported="false" />

<receiver android:name=".infrastructure.ReminderBootReceiver" android:exported="false">
<intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED" />
</intent-filter>
</receiver>
```

Use `android:exported="false"` unless another app/system must send explicit broadcasts to it. Boot
receiver needs the system broadcast, but can still usually be non-exported with the boot intent
filter.

Manifest-declared receivers are for events that can trigger app code outside the normal UI flow.

```text
Android/system or another sender may deliver this event to our app
```

For example:

```xml

<receiver android:name=".infrastructure.ReminderBootReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

Means:

```text
when device boots, Android can wake our receiver
```

For reminders:

```xml

<receiver android:name=".infrastructure.ReminderReceiver" />
```

Our own scheduled `AlarmManager` alarm sends a `PendingIntent` to that receiver.

Receivers handle events like:

- alarm fired
- device rebooted
- notification action clicked
- system event happened

Receiver code should be quick. It should usually delegate work.

## Context

`Context` gives access to Android system services and app resources.

Examples:

```kotlin
val alarmManager = context.getSystemService(AlarmManager::class.java)
val notificationManager = context.getSystemService(NotificationManager::class.java)
```

Rules:

- use application context for long-lived objects
- do not store Activity context in repositories/schedulers
- do not put `Context` in domain or features
- infrastructure can depend on `Context`

Good:

```kotlin
class AlarmReminderScheduler(
    context: Context,
) : ReminderScheduler {
    private val appContext = context.applicationContext
}
```

## ViewModel

ViewModels survive Activity recreation and own UI state.

For Noted:

- `HomeViewModel` observes notes and filters.
- `NoteEditorViewModel` owns editor fields and save/reminder flow.

Example:

```kotlin
class HomeViewModel(
    observeNotes: ObserveNotes,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        observeNotes()
            .map { notes -> HomeUiState(notes = notes.map { it.toListItem() }) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HomeUiState(),
            )
}
```

Use `viewModelScope` for coroutines started by ViewModel events.

```kotlin
fun onDelete(id: NoteId) {
    viewModelScope.launch {
        deleteNote(id)
    }
}
```

## Manual Dependency Injection

Noted starts without Hilt. Use a small app container.

```kotlin
class NotedAppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database = Room.databaseBuilder(
        appContext,
        NotedDatabase::class.java,
        "noted.db",
    ).build()

    private val repository = RoomNoteRepository(database.noteDao())
    private val reminderScheduler = AlarmReminderScheduler(appContext)
    private val clock = AndroidClock()

    val createNote = CreateNote(repository, reminderScheduler, clock)
    val updateNote = UpdateNote(repository, reminderScheduler, clock)
    val archiveNote = ArchiveNote(repository, reminderScheduler, clock)
    val deleteNote = DeleteNote(repository, reminderScheduler)
    val observeNotes = ObserveNotes(repository)
}
```

Keep wiring here. Do not construct dependencies deep inside screens or feature classes.

## Room

Room is Android's SQLite abstraction.

Noted persistence can be flatter than domain.

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val description: String,
    val reminderAtMillis: Long?,
    val status: String,
    val archivedAtMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
```

DAO:

```kotlin
@Dao
interface NoteDao {
    @Query("SELECT * FROM notes")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)
}
```

Database:

```kotlin
@Database(
    entities = [NoteEntity::class, TagEntity::class, NoteTagEntity::class],
    version = 1,
)
abstract class NotedDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun tagDao(): TagDao
}
```

Best practices:

- DAOs return `Flow` for observable data.
- DAO writes are `suspend`.
- repository maps entities to domain.
- domain does not import Room.
- store UUIDs as strings with a converter or explicit mapping.
- do not expose `NoteEntity` to UI/domain.

## Repository

Repository hides Room from features and domain.

```kotlin
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun save(note: ActiveNote)
    suspend fun delete(id: NoteId)
}
```

Room implementation lives in infrastructure.

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

## Permissions

Modern Android permissions can be runtime permissions or special settings access.

For Noted reminders:

- notification permission: runtime permission on newer Android versions
- exact alarm access: special app access setting

Architecture rule:

```text
Do not ask permissions on app start.
Ask only when user sets a reminder.
```

Flow:

```text
User selects reminder
-> check notification permission
-> request notification permission if needed
-> check exact alarm access
-> open exact alarm settings if needed
-> save reminder only if both granted
```

If either permission is denied, no reminder is saved.

## Notification Permission

Use `rememberLauncherForActivityResult` in Compose UI.

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
) { granted ->
    viewModel.onNotificationPermissionResult(granted)
}
```

Launch only in response to a user action or one-time ViewModel event.

```kotlin
LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
        when (event) {
            NoteEditorEvent.RequestNotificationPermission -> {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
```

Do not request notification permission when the app opens.

## Exact Alarm Access

Exact alarms require special handling.

Check access with `AlarmManager`:

```kotlin
val alarmManager = context.getSystemService(AlarmManager::class.java)
val canSchedule = alarmManager.canScheduleExactAlarms()
```

If not allowed, open settings:

```kotlin
val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
context.startActivity(intent)
```

After the user returns, check again. Save reminder only if access is granted.

## AlarmManager

Use exact alarms only, per architecture.

```kotlin
alarmManager.setExactAndAllowWhileIdle(
    AlarmManager.RTC_WAKEUP,
    reminderAtMillis,
    pendingIntent,
)
```

Use a stable `PendingIntent` identity so cancel works.

```kotlin
val pendingIntent = PendingIntent.getBroadcast(
    context,
    note.id.value.hashCode(),
    Intent(context, ReminderReceiver::class.java).apply {
        putExtra("note_id", note.id.value.toString())
    },
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

Cancel with the same identity.

```kotlin
alarmManager.cancel(pendingIntent)
```

Best practices:

- use `FLAG_IMMUTABLE` unless mutation is required
- use same request code and intent identity for schedule/cancel
- do not schedule past reminders
- cancel reminder when note is archived, deleted, or reminder removed
- cancel old alarm before scheduling changed reminder

## BroadcastReceiver

`BroadcastReceiver` receives system or app broadcasts.

For Noted:

- `ReminderReceiver`: alarm fired, show notification.
- `ReminderBootReceiver`: device rebooted, restore alarms.

Reminder receiver shape:

```kotlin
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getStringExtra("note_id") ?: return
        ReminderNotification.show(context, noteId)
    }
}
```

Receivers should be quick. Do not do long database work directly in `onReceive` unless carefully
handled.

For reboot restore, use a coroutine carefully or delegate to a worker-like component if needed. For
MVP, a simple controlled implementation is acceptable.

## Notifications

Notifications require a channel on Android 8+.

Create channel before showing notifications.

```kotlin
val channel = NotificationChannel(
    "reminders",
    "Reminders",
    NotificationManager.IMPORTANCE_HIGH,
)

notificationManager.createNotificationChannel(channel)
```

Show notification:

```kotlin
val notification = NotificationCompat.Builder(context, "reminders")
    .setSmallIcon(R.drawable.ic_notification)
    .setContentTitle("Note reminder")
    .setContentText(noteDescriptionPreview)
    .setPriority(NotificationCompat.PRIORITY_HIGH)
    .setAutoCancel(true)
    .build()

notificationManager.notify(noteId.hashCode(), notification)
```

Best practices:

- create channel once before notification
- keep notification text short
- tap should open relevant note if possible
- do not show notification if permission is missing
- use stable notification IDs per note

## Reboot Restore

AlarmManager alarms can be lost after device reboot.

Noted includes reboot restore in MVP.

Manifest permission:

```xml

<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Flow:

```text
Device rebooted
-> ReminderBootReceiver runs
-> load active notes with future reminders from Room
-> schedule exact alarms again
```

Only restore:

- active notes
- future reminders
- reminders that still have required access

Do not restore:

- archived notes
- deleted notes
- past reminders

## Intents

`Intent` describes an action Android should perform.

Open exact alarm settings:

```kotlin
Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
```

Open app screen from notification:

```kotlin
val intent = Intent(context, MainActivity::class.java).apply {
    putExtra("note_id", noteId.value.toString())
}
```

Use explicit intents for internal components when possible.

## PendingIntent

`PendingIntent` lets another process, such as Android system, trigger your app later.

Noted uses it for alarms and notification taps.

Alarm pending intent:

```kotlin
PendingIntent.getBroadcast(
    context,
    noteId.value.hashCode(),
    Intent(context, ReminderReceiver::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

Notification tap pending intent:

```kotlin
PendingIntent.getActivity(
    context,
    noteId.value.hashCode(),
    Intent(context, MainActivity::class.java),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)
```

Use stable request codes so replacing/canceling works.

## Date And Time

Reminders need exact date/time.

Recommended domain shape:

```kotlin
data class Reminder(
    val at: Instant,
)
```

Store in Room as epoch millis:

```kotlin
val reminderAtMillis: Long?
```

Convert at boundaries.

```kotlin
val millis = reminder.at.toEpochMilli()
val instant = Instant.ofEpochMilli(millis)
```

Keep timezone/display formatting in UI. Keep scheduling in absolute millis.

## Threads And Coroutines

Do not block the main thread.

Use suspend functions for database writes and feature actions.

```kotlin
viewModelScope.launch {
    createNote(input)
}
```

`launch` starts a coroutine.

```kotlin
viewModelScope.launch {
    createNote(input)
}
```

It means:

```text
run this block asynchronously without blocking current thread
```

`launch` itself does not return a result value. It returns a `Job`.

```kotlin
val job: Job = viewModelScope.launch {
    createNote(input)
}
```

A `Job` is for lifecycle, cancel, or wait. It is not the result of the work.

Inside `launch`, handle results by updating state or sending UI events.

```kotlin
viewModelScope.launch {
    val result = createNote(input)

    result
        .onSuccess {
            _events.send(NoteEditorEvent.NavigateBack)
        }
        .onFailure {
            _uiState.update { it.copy(error = "Could not save") }
        }
}
```

So:

```text
launch = async task
result = handled inside block
UI event = optional, manually sent
```

If you need an async result object, use `async`.

```kotlin
val deferred = async {
    repository.getNote(id)
}

val note = deferred.await()
```

Rule:

```text
launch = fire coroutine, no returned value
async = coroutine that returns value via await()
suspend call inside launch = waits sequentially
```

Room suspend DAO methods run off the main thread internally when configured normally.

For heavy work, use dispatchers intentionally.

```kotlin
withContext(Dispatchers.IO) {
    // IO work
}
```

Keep UI updates on main. ViewModel state updates are normally safe from `viewModelScope`.

## Resource Basics

Android resources live in `res/`.

Common resources:

- `res/drawable`: icons/images
- `res/mipmap`: launcher icons
- `res/values/strings.xml`: user-visible strings
- `res/values/colors.xml`: colors if not purely Compose theme
- `res/xml`: backup/config XML if needed

Prefer strings in resources for real app text if localization is planned.

For early MVP, hardcoded Compose strings are acceptable but easy to clean later.

## Build Files

Android projects use Gradle.

Typical app dependencies for Noted:

- Kotlin Android plugin
- Compose BOM
- Material 3 Compose
- Lifecycle ViewModel Compose
- Navigation Compose
- Room runtime/compiler/KTX
- Kotlin coroutines

Room compiler usually needs KSP or KAPT. Prefer KSP for new projects.

## Testing

Test layers differently.

Domain/features:

- plain JVM tests
- fast
- no Android device needed

Room:

- instrumentation tests or Robolectric
- test DAO queries and mappings

Compose UI:

- Compose UI tests for important flows
- can start minimal

Noted testing focus from architecture:

- note description validation
- active note archive transition
- archived notes cannot have reminders in domain
- archive cancels reminder
- delete cancels reminder
- reminder update reschedules alarm
- tag filtering
- archived filtering
- reboot restore schedules only active future reminders

## Best Practices For Noted

Keep Activity thin.

Keep ViewModels focused on screen state and events.

Keep Android APIs out of `domain/` and `features/`.

Put Room, alarms, receivers, notifications in `infrastructure/`.

Use app context for long-lived infrastructure objects.

Use `Flow` from Room to update UI reactively.

Use exact alarms only when both required permissions/access are granted.

Never save a reminder if notification permission or exact alarm access is denied.

Cancel alarms on archive, delete, reminder removal, and reminder change.

Restore alarms after reboot for active future reminders only.

Prefer simple manual DI until there is a real need for Hilt.

## Common Mistakes

Doing work directly in composables:

```kotlin
@Composable
fun HomeScreen() {
    repository.observeNotes()
}
```

Use ViewModel instead.

Creating databases in screens:

```kotlin
val db = Room.databaseBuilder(context, NotedDatabase::class.java, "noted.db").build()
```

Create database once in app container.

Storing Activity context in long-lived objects:

```kotlin
class AlarmReminderScheduler(private val activity: Activity)
```

Use application context.

Requesting permissions on app start.

For Noted, request only when user sets a reminder.

Forgetting notification channels.

Notifications may not show without a channel on Android 8+.

Scheduling alarms without stable `PendingIntent` identity.

Cancel/reschedule will fail if identity differs.

Putting Android imports in domain/features.

This breaks architecture boundaries.

## What To Learn First

1. Activity and lifecycle basics.
2. ViewModel and `viewModelScope`.
3. Context and application context.
4. AndroidManifest permissions and receivers.
5. Room entities, DAOs, database, migrations.
6. Runtime permissions with activity result APIs.
7. `AlarmManager` and exact alarm access.
8. `BroadcastReceiver` for reminders and reboot.
9. Notification channels and `NotificationCompat`.
10. PendingIntent identity and flags.
11. Basic Android testing split.

## Noted Implementation Checklist

```text
MainActivity hosts Compose
NotedApplication owns NotedAppContainer
Room database stores notes/tags
Repository maps Room <-> domain
HomeViewModel observes notes Flow
Editor ViewModel validates and calls features
Reminder permission asked only on reminder selection
AlarmReminderScheduler schedules/cancels exact alarms
ReminderReceiver shows notification
ReminderBootReceiver restores future active reminders
domain/features stay Android-free
```
