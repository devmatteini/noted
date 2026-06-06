package com.cosimomatteini.noted.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDescriptionTest {
    @Test
    fun parse_trimsDescription() {
        val description = NoteDescription.parse("  Buy coffee  ")

        assertEquals("Buy coffee", description.value)
    }

    @Test
    fun parse_allowsEmptyDescription() {
        val description = NoteDescription.parse("")

        assertEquals("", description.value)
    }

    @Test
    fun parse_trimsBlankDescriptionToEmpty() {
        val description = NoteDescription.parse("   ")

        assertEquals("", description.value)
    }
}
