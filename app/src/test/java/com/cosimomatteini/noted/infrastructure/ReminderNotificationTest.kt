package com.cosimomatteini.noted.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderNotificationTest {
    @Test
    fun reminderNotificationText_formatsTitleAndDescription() {
        assertEquals(
            "Title - Description",
            reminderNotificationText("Title", "Description")
        )
    }

    @Test
    fun reminderNotificationText_formatsOnlyTitle() {
        assertEquals(
            "Title",
            reminderNotificationText("Title", "")
        )
    }

    @Test
    fun reminderNotificationText_formatsOnlyDescription() {
        assertEquals(
            "Description",
            reminderNotificationText("", "Description")
        )
    }

    @Test
    fun reminderNotificationText_ellipsizesDescriptionToOneHundredChars() {
        val description = "a".repeat(101)

        assertEquals(
            "Title - ${"a".repeat(97)}...",
            reminderNotificationText("Title", description)
        )
    }
}
