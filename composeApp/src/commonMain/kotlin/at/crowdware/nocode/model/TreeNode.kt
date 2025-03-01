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

package at.crowdware.nocode.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import at.crowdware.nocodelib.utils.UIElement

enum class NodeType {
    DIRECTORY, OTHER, IMAGE, VIDEO, SOUND, XML, MD, SML, MODEL
}

val extensionToNodeType = mapOf(
    "png" to NodeType.IMAGE,
    "jpg" to NodeType.IMAGE,
    "jpeg" to NodeType.IMAGE,
    "gif" to NodeType.IMAGE,
    "mp4" to NodeType.VIDEO,
    "avi" to NodeType.VIDEO,
    "mkv" to NodeType.VIDEO,
    "mov" to NodeType.VIDEO,
    "mp3" to NodeType.SOUND,
    "wav" to NodeType.SOUND,
    "flac" to NodeType.SOUND,
    "sml" to NodeType.SML,
    "md" to NodeType.MD,
    "webp" to NodeType.IMAGE,
    "bmp" to NodeType.IMAGE,
    "webm" to NodeType.VIDEO,
    "avi" to NodeType.VIDEO,
    "flv" to NodeType.VIDEO,
    "ts" to NodeType.VIDEO,
    "3gp" to NodeType.VIDEO,
    "m4v" to NodeType.VIDEO,
    "glb" to NodeType.MODEL,
    "gltf" to NodeType.MODEL,
    "bin" to NodeType.MODEL,
    "ktx" to NodeType.MODEL,
)

open class TreeNode(
    var title: MutableState<String> = mutableStateOf(""),
    val type: Any,
    var path: String = "",
    var children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    var expanded: MutableState<Boolean> = mutableStateOf(false),
)


class ElementTreeNode(
    title: MutableState<String> = mutableStateOf(""),
    type: NodeType,
    path: String,
    children: SnapshotStateList<TreeNode> = mutableStateListOf(),
    expanded: MutableState<Boolean> = mutableStateOf(false),
    element: UIElement = UIElement.Zero
) : TreeNode(title, type, path, children, expanded)