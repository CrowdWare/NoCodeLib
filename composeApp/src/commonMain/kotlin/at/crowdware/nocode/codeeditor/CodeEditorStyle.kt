package at.crowdware.nocode.codeeditor

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import at.crowdware.nocode.theme.DarkExtendedColors
import at.crowdware.nocode.theme.ExtendedColors

data class CodeEditorStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val cursorColor: Color,
    val gutterTextColor: Color,
    val colors: ExtendedColors
)

@Composable
fun rememberCodeEditorStyle(
    backgroundColor: Color,
    textColor: Color,
    cursorColor: Color,
    gutterTextColor: Color,
): CodeEditorStyle {
    return CodeEditorStyle(
        backgroundColor = backgroundColor,
        textColor = textColor,
        cursorColor = cursorColor,
        gutterTextColor = gutterTextColor,
        colors = DarkExtendedColors
    )
}