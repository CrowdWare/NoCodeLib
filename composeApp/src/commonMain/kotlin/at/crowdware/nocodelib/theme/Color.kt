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

package at.crowdware.nocodelib.theme

import androidx.compose.ui.graphics.Color

// Light Theme Colors
val PrimaryColor = Color(0xFFDADADA)
val SecondaryColor = Color(0xFF03DAC5)
val BackgroundColor = Color(0xFFFFFFFF)
val SurfaceColor = Color(0xFFFFFFFF)
val OnPrimaryColor = Color(0xFF514F50)
val OnSecondaryColor = Color(0xFF000000)
val OnSurfaceColor = Color(0xFF000000)

// Dark Theme Colors
val PrimaryColorDark = Color(0xFF353739)
val SecondaryColorDark = Color(0xFF03DAC5)
val BackgroundColorDark = Color(0xFF121212)
val SurfaceColorDark = Color(0xFF1F1F1F)
val OnPrimaryColorDark = Color(0xFFB0B0B0)
val OnSecondaryColorDark = Color(0xFFFFFFFF)
val OnSurfaceColorDark = Color(0xFFFFFFFF)

// Extended Colors (hover states, accents, etc.)
data class ExtendedColors(
    val surfaceHoverColor: Color,
    val secondaryHoverColor: Color,
    val accentColor: Color,
    val onAccentColor: Color,
    val selectionColor: Color,
    val onSelectionColor: Color,
    val syntaxColor: Color,
    val attributeNameColor: Color,
    val attributeValueColor: Color,
    val captionColor: Color,
    val defaultTextColor: Color,
    val directoryColor: Color,
    val imageColor: Color,
    val videoColor: Color,
    val soundColor: Color,
    val xmlColor: Color,
    val mdHeader: Color,
    val linkColor: Color,
    val commentColor: Color,
    val bracketColor: Color
)

// Light Extended Colors
val LightExtendedColors = ExtendedColors(
    surfaceHoverColor = Color(0xFFE0E0E0),
    secondaryHoverColor = Color(0xFFD0D0D0),
    accentColor = Color(0xFF3468CA),
    onAccentColor = Color(0xFFFFFFFF),
    selectionColor = Color(0xFFADCEFD),
    onSelectionColor = Color(0xFFFFFFFF),
    syntaxColor = Color(0xFFB97A57),
    attributeNameColor = Color(0xFF6A5ACD),
    attributeValueColor = Color(0xFF008000),
    captionColor = Color(0xFFFCF7F3),
    defaultTextColor = Color(0xFF000000),
    directoryColor = Color(0xFF4CAF50),
    imageColor = Color(0xFFFFC107),
    videoColor = Color(0xFF2196F3),
    soundColor = Color(0xFFF44336),
    xmlColor = Color(0xFF9C27B0),
    mdHeader = Color(0xFFB774B1),
    linkColor = Color(0xFF5E90E0),
    commentColor = Color.Gray,
    bracketColor = Color(0xFFF5D52E)
)

// Dark Extended Colors
val DarkExtendedColors = ExtendedColors(
    surfaceHoverColor = Color(0xFF333333),
    secondaryHoverColor = Color(0xFF555555),
    accentColor = Color(0xFF3468CA),
    onAccentColor = Color(0xFFFFFFFF),
    selectionColor = Color(0xFF4663E9),
    onSelectionColor = Color(0xFFFFFFFF),
    syntaxColor = Color(0xFF61BEA6),
    attributeNameColor = Color(0xFFA0D4FC),
    attributeValueColor = Color(0xFFBE896F),
    captionColor = Color(0xFF37302F),
    defaultTextColor = Color(0xFFB0B0B0),
    directoryColor = Color(0xFFB0B0B0),
    imageColor = Color(0xFF64B5F6),
    videoColor = Color(0xFF94BCFD),
    soundColor = Color(0xFFF0766E),
    xmlColor = Color(0xFFA9704C),
    mdHeader = Color(0xFFB774B1),
    linkColor = Color(0xFF5C7AF1),
    commentColor = Color.Gray,
    bracketColor = Color(0xFFF5D52E)
)

fun darkenColor(color: Color, factor: Float): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}