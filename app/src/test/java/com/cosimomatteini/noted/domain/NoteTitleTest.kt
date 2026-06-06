package com.cosimomatteini.noted.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoteTitleTest {
    @Test
    fun parse_trimsTitle() {
        val title = NoteTitle.parse("  Groceries  ")

        assertEquals("Groceries", title?.value)
    }

    @Test
    fun parse_returnsNullForEmptyTitle() {
        val title = NoteTitle.parse("")

        assertNull(title)
    }

    @Test
    fun parse_returnsNullForBlankTitle() {
        val title = NoteTitle.parse("   ")

        assertNull(title)
    }

    @Test
    fun parse_returnsNullForNullTitle() {
        val title = NoteTitle.parse(null)

        assertNull(title)
    }
}
