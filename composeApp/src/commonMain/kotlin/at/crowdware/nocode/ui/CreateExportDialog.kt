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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.plugin.SmlExportPlugin
import at.crowdware.nocode.theme.ExtendedTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun createExportDialog(
    folder: TextFieldValue,
    caption: String,
    source: String,
    plugin: SmlExportPlugin,
    onFolderChange: (TextFieldValue) -> Unit,
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var jobDone by remember { mutableStateOf(false) }
    var showList by remember { mutableStateOf(false) }
    val outputLines = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    LaunchedEffect(outputLines.size) {
        if (outputLines.isNotEmpty()) {
            listState.animateScrollToItem(outputLines.size - 1)
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = caption)
        },
        text = {
            Column {
                InputRow(label = "Folder:", value = folder, onValueChange = onFolderChange, hasIcon = true)
                if (showList) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().height(500.dp)) {
                        items(outputLines) { line ->
                            Text(line, fontFamily = FontFamily.Monospace)
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
            if (!jobDone) {
                Button(
                    enabled = folder.text.isNotEmpty(),
                    onClick = {
                        showList = true
                        coroutineScope.launch(Dispatchers.IO) {
                            val folderPath = if (folder.text.endsWith(File.separator)) folder.text else folder.text + File.separator
                            val outputDir = File(folderPath)
                            outputDir.mkdirs()

                            plugin.export(source, outputDir) { line ->
                                coroutineScope.launch(Dispatchers.Main) {
                                    outputLines.add(line)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                outputLines.add("✅ Export abgeschlossen")
                                jobDone = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ExtendedTheme.colors.accentColor,
                        contentColor = ExtendedTheme.colors.onAccentColor
                    )
                ) {
                    Text("Create")
                }
            }
            if (jobDone) {
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = ExtendedTheme.colors.accentColor,
                        contentColor = ExtendedTheme.colors.onAccentColor
                    )
                ) {
                    Text("OK")
                }
            }
        }
    )
}