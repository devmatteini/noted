package com.cosimomatteini.noted.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteTitleTest {
    @Test
    fun parse_trimsTitle() {
        val title = NoteTitle.parse("  Groceries  ")

        assertEquals("Groceries", title.value)
    }

    @Test
    fun parse_allowsEmptyTitle() {
        val title = NoteTitle.parse("")

        assertEquals("", title.value)
    }

    @Test
    fun parse_trimsBlankTitleToEmpty() {
        val title = NoteTitle.parse("   ")

        assertEquals("", title.value)
    }
}
