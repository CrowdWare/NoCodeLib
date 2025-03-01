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

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf


// Define a CompositionLocal for extended colors
val LocalExtendedColors = staticCompositionLocalOf {
    LightExtendedColors // Default to light theme extended colors
}

// Helper function to access extended colors
object ExtendedTheme {
    val colors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

@Composable
fun AppTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Define Material Colors based on theme
    val colors = if (darkTheme) {
        MaterialTheme.colors.copy(
            primary = PrimaryColorDark,
            secondary = SecondaryColorDark,
            background = BackgroundColorDark,
            surface = SurfaceColorDark,
            onPrimary = OnPrimaryColorDark,
            onSecondary = OnSecondaryColorDark,
            onSurface = OnSurfaceColorDark,
        )
    } else {
        MaterialTheme.colors.copy(
            primary = PrimaryColor,
            secondary = SecondaryColor,
            background = BackgroundColor,
            surface = SurfaceColor,
            onPrimary = OnPrimaryColor,
            onSecondary = OnSecondaryColor,
            onSurface = OnSurfaceColor,
        )
    }

    // Define Extended Colors based on theme
    val extendedColors = if (darkTheme) {
        DarkExtendedColors
    } else {
        LightExtendedColors
    }

    // Provide MaterialTheme and ExtendedTheme
    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colors = colors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
