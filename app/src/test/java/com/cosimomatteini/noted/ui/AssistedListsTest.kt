package com.cosimomatteini.noted.ui

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AssistedListsTest {
    @Test
    fun addsPrefixAfterFirstItemAndMovesCursor() {
        val result = pressEnter("- one")

        assertValue(result, "- one\n- ", 8)
    }

    @Test
    fun continuesSubsequentItems() {
        val secondItem = pressEnter("- one\n- two")
        val thirdItem = pressEnter(secondItem.text + "three")

        assertValue(secondItem, "- one\n- two\n- ", 14)
        assertValue(thirdItem, "- one\n- two\n- three\n- ", 22)
    }

    @Test
    fun pressingEnterOnAutomaticEmptyBulletExitsList() {
        val automaticEmptyBullet = pressEnter("- one")

        val result = pressEnter(automaticEmptyBullet)

        assertValue(result, "- one\n", 6)
    }

    @Test
    fun pressingEnterAfterEmptyBulletDoesNotRemoveFollowingText() {
        val previous = value("- one\n- text", cursor = 8)
        val proposed = enterProposal(previous)

        assertSame(proposed, assistBulletListInput(previous, proposed))
    }

    @Test
    fun exitingListPreservesAndShiftsFollowingUrlAnnotation() {
        val previous = TextFieldValue(
            annotatedString = savedNoteDescriptionAnnotatedString("- one\n- \nhttps://example.com"),
            selection = TextRange(8)
        )

        val result = pressEnter(previous)

        assertValue(result, "- one\n\nhttps://example.com", 6)
        assertLink(result, start = 7, end = 26)
    }

    @Test
    fun continuesListAfterNormalText() {
        val result = pressEnter("intro\n- one")

        assertValue(result, "intro\n- one\n- ", 14)
    }

    @Test
    fun splitsItemAndKeepsRightTextAfterPrefix() {
        val result = pressEnter("- one two", cursor = 5)

        assertValue(result, "- one\n-  two", 8)
    }

    @Test
    fun ignoresNonListInputs() {
        listOf(
            "normal",
            "-text",
            "  - indented",
            "- "
        ).forEach { text ->
            val proposed = enterProposal(value(text))

            assertSame(proposed, assistBulletListInput(value(text), proposed))
        }
    }

    @Test
    fun ignoresSelectionReplacement() {
        val previous = value("- one", selection = TextRange(2, 5))
        val proposed = value("- \n", cursor = 3)

        assertSame(proposed, assistBulletListInput(previous, proposed))
    }

    @Test
    fun ignoresMultilinePasteAndLargerChanges() {
        val previous = value("- one")
        val paste = value("- one\n- two\n- three", cursor = 19)
        val largerChange = value("- one!\n", cursor = 7)

        assertSame(paste, assistBulletListInput(previous, paste))
        assertSame(largerChange, assistBulletListInput(previous, largerChange))
    }

    @Test
    fun acceptsPartialAndCompleteAutomaticPrefixDeletion() {
        val assisted = pressEnter("- one")
        val partialDeletion = value("- one\n-", cursor = 7)
        val completeDeletion = value("- one\n", cursor = 6)

        assertSame(partialDeletion, assistBulletListInput(assisted, partialDeletion))
        assertSame(completeDeletion, assistBulletListInput(partialDeletion, completeDeletion))
    }

    @Test
    fun normalTextAfterPrefixDeletionDoesNotContinueList() {
        val previous = value("- one\nnormal", cursor = 12)
        val proposed = enterProposal(previous)

        assertSame(proposed, assistBulletListInput(previous, proposed))
    }

    @Test
    fun explicitNewListAfterInterruptionRestartsAssistance() {
        val result = pressEnter("- one\nnormal\n- new")

        assertValue(result, "- one\nnormal\n- new\n- ", 21)
    }

    @Test
    fun preservesUrlAnnotationsBeforeInsertedPrefix() {
        val previous = TextFieldValue(
            annotatedString = savedNoteDescriptionAnnotatedString("- https://example.com"),
            selection = TextRange(21)
        )
        val result = pressEnter(previous)

        assertValue(result, "- https://example.com\n- ", 24)
        assertLink(result, start = 2, end = 21)
    }

    @Test
    fun shiftsUrlAnnotationsAfterInsertedPrefix() {
        val previous = TextFieldValue(
            annotatedString = savedNoteDescriptionAnnotatedString("- one https://example.com"),
            selection = TextRange(5)
        )
        val result = pressEnter(previous)

        assertValue(result, "- one\n-  https://example.com", 8)
        assertLink(result, start = 9, end = 28)
    }

    @Test
    fun ignoresActiveImeComposition() {
        val previous = value("- one").copy(composition = TextRange(2, 5))
        val proposed = enterProposal(previous).copy(composition = TextRange(2, 5))

        assertSame(proposed, assistBulletListInput(previous, proposed))
    }

    private fun pressEnter(text: String, cursor: Int = text.length): TextFieldValue =
        pressEnter(value(text, cursor))

    private fun pressEnter(previous: TextFieldValue): TextFieldValue =
        assistBulletListInput(previous, enterProposal(previous))

    private fun enterProposal(previous: TextFieldValue): TextFieldValue {
        val cursor = previous.selection.start
        return previous.copy(
            annotatedString = buildAnnotatedString {
                append(previous.annotatedString, start = 0, end = cursor)
                append("\n")
                append(
                    previous.annotatedString,
                    start = cursor,
                    end = previous.text.length
                )
            },
            selection = TextRange(cursor + 1),
            composition = null
        )
    }

    private fun value(
        text: String,
        cursor: Int = text.length,
        selection: TextRange = TextRange(cursor)
    ) = TextFieldValue(text = text, selection = selection)

    private fun assertValue(result: TextFieldValue, text: String, cursor: Int) {
        assertEquals(text, result.text)
        assertEquals(TextRange(cursor), result.selection)
    }

    private fun assertLink(result: TextFieldValue, start: Int, end: Int) {
        val annotation = result.annotatedString.getLinkAnnotations(start, end).single()
        assertEquals(start, annotation.start)
        assertEquals(end, annotation.end)
        assertEquals("https://example.com", (annotation.item as LinkAnnotation.Url).url)
    }
}
