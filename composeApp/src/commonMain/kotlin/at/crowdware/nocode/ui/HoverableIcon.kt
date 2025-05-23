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

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
//import androidx.compose.ui.input.pointer.pointerMoveFilter
import androidx.compose.ui.graphics.painter.Painter

enum class TooltipPosition {
    Left, Right
}


@Composable
fun HoverableIcon(
    onClick: () -> Unit,
    painter: Painter,
    tooltipText: String,
    isSelected: Boolean,
    tooltipPosition: TooltipPosition = TooltipPosition.Right
) {
    var isHovered by remember { mutableStateOf(false) }

    HoverableIconContent(
        isHovered = isHovered,
        onClick = onClick,
        painter = painter,
        tooltipText = tooltipText,
        isSelected = isSelected,
        onHoverChange = { hover -> isHovered = hover},
        tooltipPosition = tooltipPosition
    )
}

@Composable
expect fun HoverableIconContent(
    isHovered: Boolean,
    onClick: () -> Unit,
    painter: Painter,
    tooltipText: String,
    isSelected: Boolean,
    onHoverChange: (Boolean) -> Unit,
    tooltipPosition: TooltipPosition = TooltipPosition.Right
)


fun TriangleShape(flipped: Boolean = false): Shape {
    return GenericShape { size, _ ->
        if (!flipped) {
            // ▶️ zeigt nach links (Standard)
            moveTo(size.width, 0f)
            lineTo(0f, size.height / 2)
            lineTo(size.width, size.height)
        } else {
            // ◀️ zeigt nach rechts (gedreht)
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(0f, size.height)
        }
        close()
    }
}
@Composable
fun LightenColor(color: Color, lightenFactor: Float = 0.3f): Color {
    // Mischt die Farbe mit Weiß, um sie aufzuhellen (ohne Transparenz)
    return lerp(color, Color.White, lightenFactor)
}
