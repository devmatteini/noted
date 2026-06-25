package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.Instant
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportReminderPermissionTest {
    private val now = Instant.parse("2026-06-25T10:00:00Z")

    @Test
    fun nextImportPermissionAction_importsWhenNoFutureActiveReminder() {
        val action = nextImportPermissionAction(
            notes = listOf(
                activeNote(reminderAt = null),
                activeNote(reminderAt = ReminderAt(Instant.parse("2026-06-25T09:00:00Z"))),
                archivedNote(),
                discardedNote()
            ),
            now = now,
            state = deniedPermissionState()
        )

        assertEquals(ImportPermissionAction.Import, action)
    }

    @Test
    fun nextImportPermissionAction_requestsNotificationForFutureActiveReminder() {
        val action = nextImportPermissionAction(
            notes = listOf(activeNote(ReminderAt(Instant.parse("2026-06-25T11:00:00Z")))),
            now = now,
            state = ReminderPermissionState(
                notificationGranted = false,
                notificationCanRequest = true,
                exactAlarmGranted = true
            )
        )

        assertEquals(ImportPermissionAction.RequestNotification, action)
    }

    @Test
    fun nextImportPermissionAction_opensNotificationSettingsWhenRequestSuppressed() {
        val action = nextImportPermissionAction(
            notes = listOf(activeNote(ReminderAt(Instant.parse("2026-06-25T11:00:00Z")))),
            now = now,
            state = ReminderPermissionState(
                notificationGranted = false,
                notificationCanRequest = false,
                exactAlarmGranted = true
            )
        )

        assertEquals(ImportPermissionAction.OpenNotificationSettings, action)
    }

    @Test
    fun nextImportPermissionAction_opensExactAlarmSettingsWhenMissing() {
        val action = nextImportPermissionAction(
            notes = listOf(activeNote(ReminderAt(Instant.parse("2026-06-25T11:00:00Z")))),
            now = now,
            state = ReminderPermissionState(
                notificationGranted = true,
                notificationCanRequest = true,
                exactAlarmGranted = false
            )
        )

        assertEquals(ImportPermissionAction.OpenExactAlarmSettings, action)
    }

    @Test
    fun nextImportPermissionAction_importsWhenPermissionsGranted() {
        val action = nextImportPermissionAction(
            notes = listOf(activeNote(ReminderAt(Instant.parse("2026-06-25T11:00:00Z")))),
            now = now,
            state = ReminderPermissionState(
                notificationGranted = true,
                notificationCanRequest = true,
                exactAlarmGranted = true
            )
        )

        assertEquals(ImportPermissionAction.Import, action)
    }

    private fun deniedPermissionState(): ReminderPermissionState = ReminderPermissionState(
        notificationGranted = false,
        notificationCanRequest = false,
        exactAlarmGranted = false
    )

    private fun activeNote(reminderAt: ReminderAt?): ActiveNote = ActiveNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Active"),
        description = NoteDescription.of("Description"),
        reminderAt = reminderAt,
        createdAt = now,
        updatedAt = now
    )

    private fun archivedNote(): Note = ArchivedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Archived"),
        description = NoteDescription.of("Description"),
        createdAt = now,
        updatedAt = now,
        archivedAt = now
    )

    private fun discardedNote(): Note = DiscardedNote(
        id = NoteId(UUID.randomUUID()),
        title = NoteTitle.of("Discarded"),
        description = NoteDescription.of("Description"),
        createdAt = now,
        updatedAt = now,
        discardedAt = now
    )
}
