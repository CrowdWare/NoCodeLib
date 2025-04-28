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

package at.crowdware.nocode.ui

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
import at.crowdware.nocode.theme.ExtendedTheme


@Composable
fun createProjectDialog(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    id: TextFieldValue,
    onIdChange: (TextFieldValue) -> Unit,
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    theme: String,
    onThemeChanged: (String) -> Unit,
    lang: String,
    userFolder: String,
    onDismissRequest: () -> Unit,
    onCreateRequest: (String) -> Unit
) {
    val checkedStates = remember { mutableStateMapOf<String, Boolean>() }
    var projectFolder by remember { mutableStateOf(TextFieldValue("$userFolder/Apps")) }
    var folderManuallyChanged by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Create Project") },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Name:", color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(name, onNameChange, modifier = Modifier.weight(3f))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "AppId:", color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(id, onIdChange, modifier = Modifier.weight(3f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Folder:", color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(
                        projectFolder,
                        onValueChange = {
                            projectFolder = it
                            folderManuallyChanged = true
                        },
                        modifier = Modifier.weight(3f),
                        hasIcon = true
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Theme:", color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    )
                    RadioButtonItem(
                        modifier = Modifier.weight(1f),
                        label = "Light",
                        selected = theme == "Light",
                        color = MaterialTheme.colors.onPrimary,
                        onClick = { onThemeChanged("Light") }
                    )
                    RadioButtonItem(
                        modifier = Modifier.weight(1f),
                        label = "Dark",
                        selected = theme == "Dark",
                        color = MaterialTheme.colors.onPrimary,
                        onClick = { onThemeChanged("Dark") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text("Cancel")
            }
            Button(
                enabled = name.text.isNotEmpty() && projectFolder.text.isNotEmpty() && id.text.isNotEmpty(),
                onClick = {
                    onCreateRequest(projectFolder.text)
                },
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