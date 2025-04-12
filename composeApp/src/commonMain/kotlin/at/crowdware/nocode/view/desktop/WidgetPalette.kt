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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.ui.ClickableIcon
import at.crowdware.nocode.viewmodel.ProjectState
import androidx.compose.ui.res.painterResource
import at.crowdware.nocode.texteditor.state.TextEditorState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun widgetPalette(currentProject: ProjectState?, state: TextEditorState) {
    var totalHeight by remember { mutableStateOf(0f) }
    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
            .onGloballyPositioned { coordinates ->
                // Capture the full height of the Column containing TreeView and Accordion
                totalHeight = coordinates.size.height.toFloat()
            }) {
        var treeViewHeight by remember { mutableStateOf(0.5f) }

        BasicText(
            text = "Widget Palette",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f - treeViewHeight)
                .background(MaterialTheme.colors.surface) // Apply surface color here
        ) {
            LazyColumn(modifier = Modifier.background(color = MaterialTheme.colors.surface)) {
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                .pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true // Trigger hover
                                        false
                                    },
                                    onExit = {
                                        isHovered = false // Remove hover
                                        false
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Container",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ClickableIcon(
                                        painter = painterResource("drawable/row.xml"),
                                        label = "Row",
                                        sml = "    Row {\n\n}\n",
                                        state = state
                                    )
                                    ClickableIcon(
                                        //imageVector = Icons.Outlined.TextFields,
                                        painter = painterResource("drawable/column.xml"),
                                        label = "Column",
                                        sml ="    Column {\n\n}\n",
                                        state = state
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                .pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true
                                        false
                                    },
                                    onExit = {
                                        isHovered = false
                                        false
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Basic Widgets",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Column {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ClickableIcon(
                                        painter = painterResource("drawable/text.xml"),
                                        label = "Text",
                                        sml ="    Text { text: \"Lorem ipsum dolor\" }\n",
                                        state = state
                                    )
                                    ClickableIcon(
                                        painter = painterResource("drawable/markdown.xml"),
                                        label = "Markdown",
                                        sml ="    Markdown { text: \"# Header\" }\n",
                                        state = state
                                    )
                                    ClickableIcon(
                                        painter = painterResource("drawable/spacer.xml"),
                                        label = "Spacer",
                                        sml = "    Spacer { amount: 8 }\n",
                                        state = state
                                    )

                                }
                                    Row(
                                        modifier = Modifier.wrapContentSize(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        ClickableIcon(
                                            painter = painterResource("drawable/image.xml"),
                                            label = "Image",
                                            sml ="    Image { src: \"sample.png\" }\n",
                                            state = state
                                        )
                                        ClickableIcon(
                                            painter = painterResource("drawable/video.xml"),
                                            label = "Video",
                                            sml ="    Video { src: \"sample.mp4\" }\n",
                                            state = state
                                        )
                                        ClickableIcon(
                                            painter = painterResource("drawable/youtube.xml"),
                                            label = "Youtube",
                                            sml ="    Youtube { id: \"xyz\" }\n",
                                            state = state
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    var expanded by remember { mutableStateOf(true) }
                    val rotationAngle by animateFloatAsState(if (expanded) 180f else 0f)
                    var isHovered by remember { mutableStateOf(false) }

                    Column() {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = expanded.not() }
                                .padding(8.dp)
                                .pointerMoveFilter(
                                    onEnter = {
                                        isHovered = true
                                        false
                                    },
                                    onExit = {
                                        isHovered = false
                                        false
                                    }
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            BasicText(
                                "Input Widgets",
                                style = TextStyle(color = MaterialTheme.colors.onSurface),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.rotate(rotationAngle),
                                tint = MaterialTheme.colors.onSurface
                            )
                        }
                        AnimatedVisibility(visible = expanded) {
                            Box(Modifier.fillMaxWidth().padding(8.dp)) {
                                Row(
                                    modifier = Modifier.wrapContentSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ClickableIcon(
                                        painter = painterResource("drawable/button.xml"),
                                        label = "Button",
                                        sml = "    Button { label: \"Click me\" link: \"page:home\" }\n",
                                        state = state
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}