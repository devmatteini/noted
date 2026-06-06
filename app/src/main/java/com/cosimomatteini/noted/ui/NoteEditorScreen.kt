package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.ui.theme.NotedTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialTitle: String = "",
    initialDescription: String = "",
    onAutosave: suspend (title: String, description: String) -> Unit,
    onBack: suspend (title: String, description: String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var title by remember(initialTitle) { mutableStateOf(TextFieldValue(initialTitle)) }
    var description by remember(initialDescription) {
        mutableStateOf(
            TextFieldValue(
                initialDescription
            )
        )
    }
    var lastSavedTitle by remember(initialTitle) { mutableStateOf(initialTitle) }
    var lastSavedDescription by remember(initialDescription) { mutableStateOf(initialDescription) }

    LaunchedEffect(title.text, description.text) {
        if (title.text == lastSavedTitle && description.text == lastSavedDescription) {
            return@LaunchedEffect
        }
        delay(300.milliseconds)
        onAutosave(title.text, description.text)
        lastSavedTitle = title.text
        lastSavedDescription = description.text
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                onBack(title.text, description.text)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title optional") },
                singleLine = true,
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Description") },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenPreview() {
    NotedTheme {
        NoteEditorScreen(
            onAutosave = { _, _ -> },
            onBack = { _, _ -> },
        )
    }
}
