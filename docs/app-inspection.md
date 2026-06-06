# App Inspection

## Room Database

Use Android Studio App Inspection for the simplest database view.

- Run the app on an emulator or device.
- Open `View > Tool Windows > App Inspection`.
- Select process `com.cosimomatteini.noted`.
- Open `Database Inspector`.
- Select `noted.db`.
- Inspect the `notes` table.
- Enable live updates if needed.

## ADB Sqlite

Most emulators include `sqlite3`:

```bash
adb shell run-as com.cosimomatteini.noted ls databases
adb shell run-as com.cosimomatteini.noted sqlite3 databases/noted.db ".tables"
adb shell run-as com.cosimomatteini.noted sqlite3 databases/noted.db "SELECT * FROM notes;"
```

If `sqlite3` is missing on the device, pull the database locally:

```bash
adb shell run-as com.cosimomatteini.noted cp databases/noted.db /sdcard/noted.db
adb pull /sdcard/noted.db /tmp/noted.db
sqlite3 /tmp/noted.db "SELECT * FROM notes;"
```

## Clear App Data

Clear local app data, including the Room database:

```bash
make clear-app-data
```
