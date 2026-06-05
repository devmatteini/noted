package com.cosimomatteini.noted.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NoteDescriptionTest {
    @Test
    fun create_trimsDescription() {
        val description = NoteDescription.create("  Buy coffee  ")

        assertEquals("Buy coffee", description.value)
    }

    @Test
    fun create_rejectsEmptyDescription() {
        assertThrows(IllegalArgumentException::class.java) {
            NoteDescription.create("")
        }
    }

    @Test
    fun create_rejectsBlankDescription() {
        assertThrows(IllegalArgumentException::class.java) {
            NoteDescription.create("   ")
        }
    }
}
