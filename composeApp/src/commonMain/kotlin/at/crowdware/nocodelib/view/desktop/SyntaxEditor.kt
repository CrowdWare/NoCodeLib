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

package at.crowdware.nocodelib.view.desktop

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodelib.viewmodel.ProjectState
import at.crowdware.nocodelib.ui.SyntaxTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun RowScope.syntaxEditor(
    currentProject: ProjectState?,
    textFieldValue: TextFieldValue,
    onTextFieldValueChange: (TextFieldValue) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    if (currentProject != null && currentProject.isEditorVisible) {
        Column(modifier = Modifier.weight(1F).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1F).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
                BasicText(
                    text = currentProject.fileName + "",
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1,
                    style = TextStyle(color = MaterialTheme.colors.onPrimary),
                    overflow = TextOverflow.Ellipsis
                )
                SyntaxTextField(
                    onValueChange = { newValue ->
                        val oldText = textFieldValue.text
                        onTextFieldValueChange(newValue)
                        currentProject.currentFileContent = newValue
                        // don't save if only the cursor has moved (no text has changed)
                        if (oldText != newValue.text) {
                            // Automatically save the content to disk after each change
                            coroutineScope.launch(at.crowdware.nocodelib.ui.ioDispatcher()) {
                                delay(500)
                                currentProject.saveFileContent()
                                when (currentProject.path.substringAfterLast("/")) {
                                    "app.sml" -> {
                                        currentProject.loadApp()
                                    }

                                    "book.sml" -> {
                                        currentProject.loadBook()
                                    }

                                    else -> {
                                        currentProject.reloadPage()
                                    }
                                }
                            }
                        }
                    },
                    extension = currentProject.extension ?: "",
                    textFieldValue = textFieldValue
                )
            }
            if (currentProject.parseError != null) {
                CustomSelectionColors {
                    val scrollState = rememberScrollState()
                    Row (modifier = Modifier.weight(.5f)){
                        Box(
                            modifier = Modifier
                                .weight(1f) // Allow the text field to take the remaining space
                                .verticalScroll(scrollState) // Enable vertical scrolling
                        ) {
                            BasicTextField(
                                value = currentProject.parseError!!,
                                modifier = Modifier
                                    .fillMaxWidth().padding(8.dp)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(8.dp),
                                onValueChange = {},
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface,
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 200
                            )
                        }
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(scrollState),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(8.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }

                    LaunchedEffect(currentProject.parseError) {
                        delay(1000)
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().weight(1F), contentAlignment = Alignment.Center) {
            Text(text = "No file open, click a page or a part in the treeview on the left.")
        }
    }
}

@Composable
fun CustomSelectionColors(content: @Composable () -> Unit) {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.Magenta,
        backgroundColor = Color.LightGray.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        content()
    }
}