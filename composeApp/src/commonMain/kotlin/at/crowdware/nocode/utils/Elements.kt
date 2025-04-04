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

package at.crowdware.nocode.utils

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime


data class App(
    @StringAnnotation("Name of the book.")
    var name: String = "",
    @StringAnnotation("Put a description about the book here.")
    var description: String = "",
    @StringAnnotation("Icon for the book. Sample: icon.png")
    var icon: String = "",
    @StringAnnotation("Unique Id of the app. Sample: com.example.bookname")
    var id: String = "",
    @StringAnnotation("Version of the current SML. default is 1.1")
    var smlVersion: String = "1.1",
    @StringAnnotation("The name of the author.")
    var author: String = "",
    @StringAnnotation("A short bio about the author.")
    var theme: ThemeElement = ThemeElement(),
    var deployment: DeploymentElement = DeploymentElement()
)

data class ThemeElement(
    var primary: String = "",
    var onPrimary: String = "",
    var primaryContainer: String = "",
    var onPrimaryContainer: String = "",
    var secondary: String = "",
    var onSecondary: String = "",
    var secondaryContainer: String = "",
    var onSecondaryContainer: String = "",
    var tertiary: String = "",
    var onTertiary: String = "",
    var tertiaryContainer: String = "",
    var onTertiaryContainer: String = "",
    var error: String = "",
    var errorContainer: String = "",
    var onError: String = "",
    var onErrorContainer: String = "",
    var background: String = "",
    var onBackground: String = "",
    var surface: String = "",
    var onSurface: String = "",
    var surfaceVariant: String = "",
    var onSurfaceVariant: String = "",
    var outline: String = "",
    var inverseOnSurface: String = "",
    var inverseSurface: String = "",
    var inversePrimary: String = "",
    var surfaceTint: String = "",
    var outlineVariant: String = "",
    var scrim: String = ""
)

data class DeploymentElement(
    val files: MutableList<FileElement> = mutableListOf()
)

data class FileElement(val path: String, val time: LocalDateTime)

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)


data class MarkdownElement(
    @MarkdownAnnotation
    val text: String = "",

    @StringAnnotation("Name of the part (from the ebook project) which will be inserted here. Like: home.md")
    val part: String = "",

    @HexColorAnnotation
    val color: String = "onPrimary",

    @IntAnnotation
    val fontSize: TextUnit = 16.sp,

    @StringAnnotation("Enter one of the following weights normal, extralight, thin, medium, bold, semibold, extrabold, black like **fontWeight: \"bold\"**")
    val fontWeight: FontWeight = FontWeight.Normal,

    @StringAnnotation("Enter one of the following alignments left, center, right like **textAlign: \"center\"**")
    val textAlign: TextAlign = TextAlign.Start,

    @WeightAnnotation
    val weight: Int = 0,

    @IntAnnotation
    val width: Int = 0,

    @IntAnnotation
    val height: Int = 0,

    )

fun fillThemeFromSmlNode(themeNode: SmlNode): ThemeElement {
    val theme = ThemeElement()

    // Durch die Properties des Theme-Nodes iterieren
    themeNode.properties.forEach { (key, value) ->
        when (key) {
            "primary" -> theme.primary = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onPrimary" -> theme.onPrimary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "background" -> theme.background = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onBackground" -> theme.onBackground = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "secondary" -> theme.secondary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onSecondary" -> theme.onSecondary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "secondaryContainer" -> theme.secondaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onSecondaryContainer" -> theme.onSecondaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "tertiary" -> theme.tertiary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onTertiary" -> theme.onTertiary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "tertiaryContainer" -> theme.tertiaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onTertiaryContainer" -> theme.onTertiaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "error" -> theme.error = (value as? PropertyValue.StringValue)?.value ?: "#FF0000"
            "onError" -> theme.onError = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "errorContainer" -> theme.errorContainer = (value as? PropertyValue.StringValue)?.value ?: "#FF0000"
            "onErrorContainer" -> theme.onErrorContainer = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "surface" -> theme.surface = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onSurface" -> theme.onSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "surfaceVariant" -> theme.surfaceVariant = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onSurfaceVariant" -> theme.onSurfaceVariant = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "outline" -> theme.outline = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "outlineVariant" -> theme.outlineVariant = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "inversePrimary" -> theme.inversePrimary = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "inverseSurface" -> theme.inverseSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "inverseOnSurface" -> theme.inverseOnSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "surfaceTint" -> theme.surfaceTint = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "scrim" -> theme.scrim = (value as? PropertyValue.StringValue)?.value ?: "#000000"
        }
    }

    return theme
}

fun fillAppFromSmlNode(appNode: SmlNode): App {
    val app = App()

    appNode.properties.forEach { (key, value) ->
        when (key) {
            "name" -> app.name = (value as? PropertyValue.StringValue)?.value ?: ""
            "description" -> app.description = (value as? PropertyValue.StringValue)?.value ?: ""
            "icon" -> app.icon = (value as? PropertyValue.StringValue)?.value ?: ""
            "id" -> app.id = (value as? PropertyValue.StringValue)?.value ?: ""
            "smlVersion" -> app.smlVersion = (value as? PropertyValue.StringValue)?.value ?: "1.1"
            "author" -> app.author = (value as? PropertyValue.StringValue)?.value ?: ""
        }
    }

    val themeNode = appNode.children.find { it.name == "Theme" }
    app.theme = themeNode?.let { fillThemeFromSmlNode(it) } ?: ThemeElement()
    return app
}

