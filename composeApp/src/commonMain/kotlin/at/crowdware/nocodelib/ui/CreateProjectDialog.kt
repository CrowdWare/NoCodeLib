/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import at.crowdware.nocodelib.theme.ExtendedTheme


@Composable
fun createProjectDialog(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    folder: TextFieldValue,
    onFolderChange: (TextFieldValue) -> Unit,
    id: TextFieldValue,
    onIdChange: (TextFieldValue) -> Unit,
    theme: String,
    onThemeChanged: (String) -> Unit,
    onCheckBookChanged: (Boolean) -> Unit,
    onCheckAppChanged: (Boolean) -> Unit,
    app: Boolean,
    book: Boolean,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create Project")
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Type:",
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f))
                    CheckboxItem(
                        modifier = Modifier.weight(1f),
                        checked = book,
                        onCheckedChange = onCheckBookChanged,
                        label = "Ebook", color = MaterialTheme.colors.onPrimary)
                    CheckboxItem(modifier = Modifier.weight(1f),
                        checked = app,
                        onCheckedChange = onCheckAppChanged,
                        label = "App", color = MaterialTheme.colors.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Name:",
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(name, onNameChange, modifier = Modifier.weight(3F))
                }
                Spacer(modifier = Modifier.height(16.dp))
                if(app) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "AppId:",
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                        Spacer(modifier = Modifier.width(16.dp))
                        TextInput(id, onIdChange, modifier = Modifier.weight(3F))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Folder:",
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1F))
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(folder, onFolderChange, modifier = Modifier.weight(3F), hasIcon = true)
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (app) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Theme:",
                            color = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.align(Alignment.CenterVertically).weight(1f))
                        RadioButtonItem(modifier = Modifier.weight(1f),
                            label = "Light",
                            selected = theme == "Light",
                            color = MaterialTheme.colors.onPrimary,
                            onClick = { onThemeChanged("Light") }
                        )
                        RadioButtonItem(modifier = Modifier.weight(1f),
                            label = "Dark",
                            selected = theme == "Dark",
                            color = MaterialTheme.colors.onPrimary,
                            onClick = { onThemeChanged("Dark") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text("Cancel")
            }
            Button(
                enabled = (app || book) && name.text.isNotEmpty() && folder.text.isNotEmpty() && id.text.isNotEmpty(),
                onClick = onCreateRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ExtendedTheme.colors.accentColor,
                    contentColor = ExtendedTheme.colors.onAccentColor
                )
            ) {
                Text("Create")
            }
        }
    )
}
