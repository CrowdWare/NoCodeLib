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

package at.crowdware.nocode.view.desktop

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
import at.crowdware.nocode.viewmodel.ProjectState
import at.crowdware.nocode.texteditor.codeeditor.CodeEditor
import at.crowdware.nocode.texteditor.codeeditor.rememberCodeEditorStyle
import at.crowdware.nocode.texteditor.state.SpanClickType
import at.crowdware.nocode.texteditor.state.TextEditorState
import kotlinx.coroutines.delay


@Composable
fun RowScope.syntaxEditor(
    currentProject: ProjectState?,
    state: TextEditorState
) {
    val relative = currentProject?.folder?.let { currentProject.path.removePrefix(it).removePrefix("/") }
    if (currentProject != null && currentProject.isEditorVisible) {
        Column(modifier = Modifier.weight(1F).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1F).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
                BasicText(
                    text = relative.toString(),
                    modifier = Modifier.padding(8.dp),
                    maxLines = 1,
                    style = TextStyle(color = MaterialTheme.colors.onPrimary),
                    overflow = TextOverflow.Ellipsis
                )
                val style = rememberCodeEditorStyle(
                    placeholderText = "Enter text here",
                    placeholderColor = MaterialTheme.colors.onPrimary,
                    backgroundColor = MaterialTheme.colors.surface,
                    cursorColor = MaterialTheme.colors.onSurface,
                    gutterTextColor = MaterialTheme.colors.onPrimary,
                )

                CodeEditor(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                    state = state,
                    style = style,
                    onRichSpanClick = { span, clickType, _ ->
                        when (clickType) {
                            SpanClickType.TAP -> println("Touch tap on span: $span")
                            SpanClickType.PRIMARY_CLICK -> println("Left click on span: $span")
                            SpanClickType.SECONDARY_CLICK -> println("Right click on span: $span")
                        }
                        true
                    })
                LaunchedEffect(Unit) {
                    state.editOperations.collect { operation ->
                        val newText = state.getAllText().text
                        val oldText = currentProject.currentFileContent.text ?: ""
                        currentProject.currentFileContent = TextFieldValue(newText)

                        if (oldText != newText) {
                            delay(500)
                            currentProject.saveFileContent()
                            when (currentProject.path.substringAfterLast("/")) {
                                "app.sml" -> {
                                    currentProject.loadApp()
                                }
                                else -> {
                                    currentProject.reloadPage()
                                }
                            }
                        }
                    }
                }
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
        Box(modifier = Modifier.fillMaxSize().weight(1F).padding(8.dp), contentAlignment = Alignment.Center) {
            Text(color = MaterialTheme.colors.onPrimary , text = "No file open, click a page or a part in the treeview on the left if a project is open. If no project is open then click the OPEN project icon in the toolbar on the left or CREATE a new project clicking the plus icon in the toolbar on the left.")
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
