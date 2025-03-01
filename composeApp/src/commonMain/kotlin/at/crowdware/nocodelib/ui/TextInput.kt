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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodelib.theme.ExtendedTheme
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker


@Composable
fun TextInput(
    text: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hasIcon: Boolean = false) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var showDirectoryPicker by remember { mutableStateOf(false) }

    val borderColor = if (isFocused) {
        ExtendedTheme.colors.accentColor
    } else {
        MaterialTheme.colors.onPrimary
    }

    val customSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.secondary.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        Row(
            modifier = modifier.border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
        ) {
            BasicTextField(
                value = text,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colors.onPrimary,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                modifier = modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.primary)
                    .padding(4.dp)
                    .focusable(interactionSource = interactionSource),
                interactionSource = interactionSource
            )
            if (hasIcon) {
                IconButton(onClick = {}, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Folder,
                        contentDescription = "Folder",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.clickable {
                        showDirectoryPicker = true
                    })
                }
            }
        }
    }

    DirectoryPicker(showDirectoryPicker, title = "Pick a folder") { path ->
        if (!path.isNullOrEmpty()) {
            showDirectoryPicker = false
            onValueChange(TextFieldValue(path))
        }
    }
}

