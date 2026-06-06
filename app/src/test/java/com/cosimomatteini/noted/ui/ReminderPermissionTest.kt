package com.cosimomatteini.noted.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderPermissionTest {
    @Test
    fun nextReminderPermissionAction_requestsNotificationWhenMissing() {
        val action = nextReminderPermissionAction(
            ReminderPermissionState(
                notificationGranted = false,
                notificationCanRequest = true,
                exactAlarmGranted = true
            )
        )

        assertEquals(ReminderPermissionAction.RequestNotification, action)
    }

    @Test
    fun nextReminderPermissionAction_opensNotificationSettingsWhenRequestSuppressed() {
        val action = nextReminderPermissionAction(
            ReminderPermissionState(
                notificationGranted = false,
                notificationCanRequest = false,
                exactAlarmGranted = true
            )
        )

        assertEquals(ReminderPermissionAction.OpenNotificationSettings, action)
    }

    @Test
    fun nextReminderPermissionAction_opensExactAlarmSettingsWhenMissing() {
        val action = nextReminderPermissionAction(
            ReminderPermissionState(
                notificationGranted = true,
                notificationCanRequest = true,
                exactAlarmGranted = false
            )
        )

        assertEquals(ReminderPermissionAction.OpenExactAlarmSettings, action)
    }

    @Test
    fun nextReminderPermissionAction_savesOnlyWhenAllPermissionsGranted() {
        val action = nextReminderPermissionAction(
            ReminderPermissionState(
                notificationGranted = true,
                notificationCanRequest = true,
                exactAlarmGranted = true
            )
        )

        assertEquals(ReminderPermissionAction.SaveReminder, action)
    }
}
