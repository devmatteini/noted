package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.ui.theme.NotedTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    screenTitle: String,
    initialTitle: String = "",
    initialDescription: String = "",
    onSave: (title: String, description: String) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember(initialTitle) { mutableStateOf(TextFieldValue(initialTitle)) }
    var description by remember(initialDescription) { mutableStateOf(TextFieldValue(initialDescription)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel")
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
                supportingText = { Text("Required") },
            )
            Button(
                onClick = { onSave(title.text, description.text) },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.text.trim().isNotEmpty(),
            ) {
                Text("Save")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoteEditorScreenPreview() {
    NotedTheme {
        NoteEditorScreen(
            screenTitle = "Create note",
            onSave = { _, _ -> },
            onCancel = {},
        )
    }
}
