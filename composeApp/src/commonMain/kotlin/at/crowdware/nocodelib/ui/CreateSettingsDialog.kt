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

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import at.crowdware.nocodelib.theme.ExtendedTheme

@Composable
fun settingsDialog(
    license: TextFieldValue,
    onLicenseChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit,
    onCreateRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "Settings")
        },
        text = {
            Column(modifier = Modifier.padding(16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()){
                    Text(
                        text = "LicenseKey:",
                        color = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.align(Alignment.CenterVertically).weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextInput(license, onLicenseChange, modifier = Modifier.weight(3F))
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
                onClick = onCreateRequest,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = ExtendedTheme.colors.accentColor,
                    contentColor = ExtendedTheme.colors.onAccentColor
                )
            ) {
                Text("Apply")
            }
        }
    )
}
