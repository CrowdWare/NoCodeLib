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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.theme.ExtendedTheme
import at.crowdware.nocode.viewmodel.GlobalAppState
import at.crowdware.nocode.viewmodel.LicenseType
import java.awt.Desktop
import java.net.URI

@Composable
fun createEbookDialog(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    folder: TextFieldValue,
    lang: String,
    onFolderChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    //onCreateRequest: () -> Unit
    onCreateRequest: (List<String>) -> Unit
) {

    val checkedStates = remember { mutableStateMapOf<String, Boolean>() }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Create Ebook")
        },
        text = {
            Column {
                InputRow(label = "Name:", value = name, onValueChange = onNameChange)
                Spacer(modifier = Modifier.height(16.dp))
                InputRow(label = "Folder:", value = folder, onValueChange = onFolderChange, hasIcon = true)

                val languageList = lang.split(",").map { it.trim() }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Languages")
                Column {
                    languageList.forEach { lang ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Checkbox(
                                checked = checkedStates[lang] ?: false,
                                onCheckedChange = { isChecked -> checkedStates[lang] = isChecked },
                                colors = CheckboxDefaults.colors(checkedColor = ExtendedTheme.colors.accentColor)
                            )
                            Text(text = lang.uppercase(), modifier = Modifier.padding(start = 8.dp))
                        }
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
                enabled = name.text.isNotEmpty() && folder.text.isNotEmpty(),
                onClick = {
                    // Filtere nur die aktivierten Sprachen heraus und übergebe sie
                    val selectedLanguages = checkedStates.filterValues { it }.keys.toList()
                    onCreateRequest(selectedLanguages)
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

@Composable
fun ClickableText(text: String, url: String) {
    Text(
        text = text,
        modifier = Modifier.clickable {
            openInBrowser(url)
        },
        color = ExtendedTheme.colors.linkColor,
        style = androidx.compose.ui.text.TextStyle(textDecoration = TextDecoration.Underline)
    )
}

fun openInBrowser(url: String) {
    try {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            println("Desktop not supported")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}