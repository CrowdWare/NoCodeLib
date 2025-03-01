
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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import at.crowdware.nocodelib.theme.ExtendedTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit
) {
    val lightenedBackgroundColor = LightenColor(MaterialTheme.colors.primary, 0.1f)

    Box(modifier = Modifier
        .size(48.dp)
        .pointerMoveFilter(
            onEnter = {
                onHoverChange(true)
                false
            },
            onExit = {
                onHoverChange(false)
                false
            }
        ).clickable { onClick() }
    ) {
        Icon(
            painter = painter,
            contentDescription = "Hoverable Icon",
            tint = if (isHovered || isSelected) ExtendedTheme.colors.accentColor else MaterialTheme.colors.onPrimary,
            modifier = Modifier.size(32.dp).align(Alignment.Center)
        )
        if (isHovered) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(38, 8),
                properties = PopupProperties(focusable = false)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp, 16.dp)
                            .background(lightenedBackgroundColor, TriangleShape())
                    )
                    Box(
                        modifier = Modifier
                            .background(lightenedBackgroundColor, shape = RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        BasicText(
                            text = tooltipText,
                            style = TextStyle(
                                color = MaterialTheme.colors.onSurface,
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}