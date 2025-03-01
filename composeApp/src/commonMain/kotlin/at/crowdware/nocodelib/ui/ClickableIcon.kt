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

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerMoveFilter
//import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodelib.theme.ExtendedTheme
import at.crowdware.nocodelib.utils.uiStates
import at.crowdware.nocodelib.viewmodel.GlobalProjectState
import at.crowdware.nocodelib.viewmodel.ProjectState


@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun ClickableIcon(
    painter: Painter,
    label: String,
    sml: String
) {
    val currentState = uiStates.current
    val currentProject: ProjectState = GlobalProjectState.projectState!!
    var dragShadow by remember { mutableStateOf(1f) }
    var isHovered by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isDarkTheme = MaterialTheme.colors.isLight.not()
    val iconTint = if (isHovered) {
        ExtendedTheme.colors.accentColor
    } else {
        if (isDarkTheme) {
            MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colors.onSurface
        }
    }

    // Box background and border color based on hover and theme
    val backgroundColor = if (isHovered) {
        if (isDarkTheme) MaterialTheme.colors.surface.copy(alpha = 0.1f)
        else MaterialTheme.colors.surface.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val borderColor = if (isHovered) {
        ExtendedTheme.colors.accentColor
    } else {
        if (isDarkTheme) MaterialTheme.colors.onSurface.copy(alpha = 0.2f)
        else MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable {
               //val cursorPosition = currentProject.editor.caretPosition
                //currentProject.editor.insert(sml, cursorPosition)
                //currentProject.editor.caretPosition = cursorPosition + sml.length
            }
            .width(95.dp)
            .border(2.dp, borderColor, shape = RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(8.dp)
            .hoverable(interactionSource = interactionSource)
            .onGloballyPositioned {
                currentState.objectLocalPosition = it.localToWindow(Offset.Zero)
            }.pointerMoveFilter(
                onEnter = {
                    isHovered = true
                    false
                },
                onExit = {
                    isHovered = false
                    false
                }
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                painter = painter,
                contentDescription = "",
                modifier = Modifier
                    .size(48.dp)
                    .alpha(dragShadow),
                tint = iconTint
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(modifier = Modifier.alpha(dragShadow),
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.body1
            )
        }
    }
}