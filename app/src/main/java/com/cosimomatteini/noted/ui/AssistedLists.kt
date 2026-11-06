package com.cosimomatteini.noted.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue

private const val BULLET_LIST_PREFIX = "- "

internal fun assistBulletListInput(
    previousDescription: TextFieldValue,
    proposedDescription: TextFieldValue
): TextFieldValue {
    if (hasSelectionOrImeComposition(previousDescription, proposedDescription)) {
        return proposedDescription
    }

    val previousCursorOffset = previousDescription.selection.start
    if (!isSingleNewlineInsertedAtCursor(
            previousDescription,
            proposedDescription,
            previousCursorOffset
        )
    ) {
        return proposedDescription
    }

    val currentLineStartOffset =
        previousDescription.text.lastIndexOf('\n', startIndex = previousCursorOffset - 1) + 1
    val nextNewlineOffset = previousDescription.text.indexOf(
        '\n',
        startIndex = previousCursorOffset
    )
    val currentLineEndOffset =
        if (nextNewlineOffset == -1) previousDescription.text.length else nextNewlineOffset
    val currentLine = previousDescription.text.substring(
        currentLineStartOffset,
        currentLineEndOffset
    )
    val currentLineBeforeCursor =
        previousDescription.text.substring(currentLineStartOffset, previousCursorOffset)
    if (isEmptyBulletAfterListItem(
            previousDescription.text,
            currentLine,
            currentLineStartOffset
        )
    ) {
        return removeEmptyBulletListItem(
            previousDescription,
            currentLineStartOffset,
            previousCursorOffset
        )
    }
    if (!isNonEmptyBulletListItem(currentLineBeforeCursor)) {
        return proposedDescription
    }

    val newLineStartOffset = previousCursorOffset + 1
    val descriptionWithNextBullet = buildAnnotatedString {
        append(proposedDescription.annotatedString, start = 0, end = newLineStartOffset)
        append(BULLET_LIST_PREFIX)
        append(
            proposedDescription.annotatedString,
            start = newLineStartOffset,
            end = proposedDescription.annotatedString.length
        )
    }

    return TextFieldValue(
        annotatedString = descriptionWithNextBullet,
        selection = TextRange(newLineStartOffset + BULLET_LIST_PREFIX.length)
    )
}

private fun removeEmptyBulletListItem(
    previousDescription: TextFieldValue,
    currentLineStartOffset: Int,
    previousCursorOffset: Int
): TextFieldValue {
    val descriptionWithoutEmptyBullet = buildAnnotatedString {
        append(previousDescription.annotatedString, start = 0, end = currentLineStartOffset)
        append(
            previousDescription.annotatedString,
            start = previousCursorOffset,
            end = previousDescription.annotatedString.length
        )
    }

    return TextFieldValue(
        annotatedString = descriptionWithoutEmptyBullet,
        selection = TextRange(currentLineStartOffset)
    )
}

private fun hasSelectionOrImeComposition(
    previousDescription: TextFieldValue,
    proposedDescription: TextFieldValue
): Boolean = !previousDescription.selection.collapsed ||
    previousDescription.composition != null ||
    proposedDescription.composition != null

private fun isSingleNewlineInsertedAtCursor(
    previousDescription: TextFieldValue,
    proposedDescription: TextFieldValue,
    previousCursorOffset: Int
): Boolean {
    val expectedTextAfterNewline =
        previousDescription.text.substring(0, previousCursorOffset) +
            "\n" +
            previousDescription.text.substring(previousCursorOffset)

    return proposedDescription.text == expectedTextAfterNewline &&
        proposedDescription.selection.collapsed &&
        proposedDescription.selection.start == previousCursorOffset + 1
}

private fun isNonEmptyBulletListItem(lineBeforeCursor: String): Boolean =
    lineBeforeCursor.startsWith(BULLET_LIST_PREFIX) &&
        lineBeforeCursor.drop(BULLET_LIST_PREFIX.length).isNotBlank()

private fun isEmptyBulletAfterListItem(
    description: String,
    currentLine: String,
    currentLineStartOffset: Int
): Boolean {
    if (currentLine != BULLET_LIST_PREFIX || currentLineStartOffset == 0) {
        return false
    }

    val previousLineEndOffset = currentLineStartOffset - 1
    val previousLineStartOffset =
        description.lastIndexOf('\n', startIndex = previousLineEndOffset - 1) + 1
    val previousLine = description.substring(previousLineStartOffset, previousLineEndOffset)

    return isNonEmptyBulletListItem(previousLine)
}
