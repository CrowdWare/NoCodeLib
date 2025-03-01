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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import at.crowdware.nocodelib.theme.ExtendedTheme
import at.crowdware.nocodelib.viewmodel.GlobalAppState
import at.crowdware.nocodelib.viewmodel.LicenseType
import java.awt.Desktop
import java.net.URI

@Composable
fun createEbookDialog(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    folder: TextFieldValue,
    onFolderChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {
    if(GlobalAppState.appState?.licenseType == LicenseType.UNDEFINED) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = "License")
            },
            text = {
                Column {
                    Text(text = "No License key entered.\nPlease open settings and enter a valid license key.\nYou can get the license key on our website.")
                    ClickableText(text = "https://freebook.crowdware.at/abo.html", url = "https://freebook.crowdware.at/abo.html")
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest
                ) {
                    Text("Cancel")
                }
            })
    } else if (GlobalAppState.appState?.licenseType == LicenseType.EXPIRED) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = "License expired")
            },
            text = {
                Column {
                    Text(text = "License expired.\nPlease open settings and enter a valid license key.\nYou can get a new license key on our website.")
                    ClickableText(text = "https://freebook.crowdware.at/abo.html", url = "https://freebook.crowdware.at/abo.html")
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissRequest
                ) {
                    Text("Cancel")
                }
            })
    } else {
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