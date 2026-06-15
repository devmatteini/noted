package com.cosimomatteini.noted.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cosimomatteini.noted.domain.ReminderAt
import java.time.Instant
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NoteDetailsScaffold(
    onBack: () -> Unit,
    content:
    @Composable()
    ((PaddingValues) -> Unit)
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = content
    )
}

@Composable
internal fun NoteDetailsContentColumn(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    imePadding: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val imeModifier = if (imePadding) Modifier.imePadding() else Modifier

    Column(
        modifier = modifier
            .padding(innerPadding)
            .fillMaxSize()
            .navigationBarsPadding()
            .then(imeModifier)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

@Composable
internal fun NoteActionsRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
internal fun noteTitleTextStyle(): TextStyle = TextStyle(
    color = MaterialTheme.colorScheme.onSurface,
    fontSize = 24.sp
)

@Composable
internal fun noteDescriptionTextStyle(): TextStyle = TextStyle(
    color = MaterialTheme.colorScheme.onSurface,
    fontSize = 16.sp
)

@Composable
internal fun TextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean = false
) {
    BaseTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        textStyle = textStyle,
        singleLine = singleLine
    )
}

@Composable
internal fun RichTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean = false
) {
    val uriHandler = LocalUriHandler.current
    var textLayoutResult: TextLayoutResult? by remember { mutableStateOf(null) }
    val linkClickModifier = Modifier.openNoteUrlOnTap(
        textLayoutResult = { textLayoutResult },
        text = value.annotatedString,
        onUrlClick = uriHandler::openUri
    )

    BaseTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.then(linkClickModifier),
        placeholder = placeholder,
        textStyle = textStyle,
        singleLine = singleLine,
        onTextLayout = { textLayoutResult = it }
    )
}

@Composable
internal fun ReadOnlyTextField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    singleLine: Boolean = false
) {
    var textFieldValue by remember(value.annotatedString) { mutableStateOf(value) }

    BaseTextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = modifier,
        placeholder = "",
        textStyle = textStyle,
        singleLine = singleLine,
        readOnly = true
    )
}

@Composable
internal fun ReadOnlyRichTextField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    textStyle: TextStyle,
    singleLine: Boolean = false
) {
    val uriHandler = LocalUriHandler.current
    var textFieldValue by remember(value.annotatedString) { mutableStateOf(value) }
    var textLayoutResult: TextLayoutResult? by remember { mutableStateOf(null) }
    val linkClickModifier = Modifier.openNoteUrlOnTap(
        textLayoutResult = { textLayoutResult },
        text = textFieldValue.annotatedString,
        onUrlClick = uriHandler::openUri
    )

    BaseTextField(
        value = textFieldValue,
        onValueChange = { textFieldValue = it },
        modifier = modifier.then(linkClickModifier),
        placeholder = "",
        textStyle = textStyle,
        singleLine = singleLine,
        readOnly = true,
        onTextLayout = { textLayoutResult = it }
    )
}

@Composable
private fun BaseTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    textStyle: TextStyle,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle,
        singleLine = singleLine,
        readOnly = readOnly,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
        onTextLayout = onTextLayout,
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        style = textStyle
                    )
                }
                innerTextField()
            }
        }
    )
}

private fun Modifier.openNoteUrlOnTap(
    textLayoutResult: () -> TextLayoutResult?,
    text: AnnotatedString,
    onUrlClick: (String) -> Unit
): Modifier = pointerInput(text, onUrlClick) {
    awaitEachGesture {
        val down = awaitFirstDown(pass = PointerEventPass.Initial)
        val layoutResult = textLayoutResult()
        val offset = layoutResult?.getOffsetForPosition(down.position)
        val url = offset?.let {
            val link = text.getLinkAnnotations(
                start = it,
                end = it
            ).firstOrNull()?.item as? LinkAnnotation.Url
            link?.url
        }
        if (url == null) {
            return@awaitEachGesture
        }

        val up = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
            waitForUpOrCancellation(pass = PointerEventPass.Initial)
        }
        if (up != null) {
            up.consume()
            onUrlClick(url)
        }
    }
}

@Composable
internal fun NoteActionIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
internal fun NoteReminderChip(
    reminderAt: ReminderAt,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPast = reminderAt.value < Instant.now()

    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPast) Icons.Filled.AlarmOff else Icons.Filled.Alarm,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = reminderAt.formatReminderChipDateTime(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (isPast) TextDecoration.LineThrough else null
        )
    }
}
