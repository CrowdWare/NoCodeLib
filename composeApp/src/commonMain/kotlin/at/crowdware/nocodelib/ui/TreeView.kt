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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocode.model.NodeType
import at.crowdware.nocode.model.TreeNode



@Composable
fun TreeView(
    tree: List<TreeNode>,
    iconProvider: @Composable ((TreeNode) -> Unit)? = null,
    onNodeDoubleClick: (TreeNode) -> Unit,
    onNodeRightClick: (TreeNode, Offset, Offset) -> Unit,
    onClick: (TreeNode) -> Unit
) {
    val listState = rememberLazyListState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colors.surface)
        ) {
            items(tree.size) { index ->
                TreeNodeView(
                    node = tree[index],
                    iconProvider = iconProvider,
                    onDoubleClick = onNodeDoubleClick,
                    onRightClick = onNodeRightClick,
                    onClick = onClick,
                )
            }
        }

        // Conditionally show the scrollbar only when the content is scrollable
        if (listState.canScrollForward || listState.canScrollBackward) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 4.dp)
            )
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TreeNodeView(
    node: TreeNode,
    level: Int = 0,
    iconProvider: @Composable ((TreeNode) -> Unit)? = null,
    onDoubleClick: (TreeNode) -> Unit,
    onRightClick: (TreeNode, Offset, Offset) -> Unit,
    onClick: (TreeNode) -> Unit
) {
    val rotationAngle by animateFloatAsState(if (node.expanded.value) 0f else -90f)
    var isHovered by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    val doubleClickInterval = 300L
    var treeNodeOffset by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                treeNodeOffset = coordinates.positionInWindow()
            }
    ) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerMoveFilter(
                        onEnter = {
                            isHovered = true
                            false
                        },
                        onExit = {
                            isHovered = false
                            false
                        }
                    )
                    .pointerHoverIcon(if (isHovered) PointerIcon.Hand else PointerIcon.Default)
                    .pointerInput(node) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                val position = event.changes.first().position
                                if (event.buttons.isSecondaryPressed) {
                                    onRightClick(node, treeNodeOffset, position)
                                } else if (event.buttons.isPrimaryPressed) {
                                    val currentClickTime = System.currentTimeMillis()
                                    if (currentClickTime - lastClickTime < doubleClickInterval) {
                                        onDoubleClick(node)
                                    }
                                    lastClickTime = currentClickTime
                                    onClick(node)

                                    // Expand directory on left-click
                                    if (node.type == NodeType.DIRECTORY) {
                                        node.expanded.value = !node.expanded.value
                                    }
                                }
                            }
                        }
                    }
                    .padding(start = (level * 16).dp, top = 4.dp, bottom = 4.dp)
            ) {
                Spacer(modifier = Modifier.width((level * 16).dp))
                if (node.type == NodeType.DIRECTORY) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier
                            .rotate(rotationAngle)
                            .size(16.dp)
                    )
                    //Spacer(modifier = Modifier.width(6.dp))
                } else if (level == 0) {
                    Spacer(modifier = Modifier.width(16.dp))
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
                iconProvider?.let {
                    it(node)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    node.title.value,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
        AnimatedVisibility(visible = node.expanded.value) {
            Column {
                node.children.forEach { childNode ->
                    TreeNodeView(
                        node = childNode,
                        level = level + 1,
                        iconProvider = iconProvider,
                        onDoubleClick = onDoubleClick,
                        onRightClick = onRightClick,
                        onClick = onClick
                    )
                }
            }
        }
    }
}



