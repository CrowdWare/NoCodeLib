package at.crowdware.nocode.codeeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import java.io.File

@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    filePath: String,
    style: CodeEditorStyle
) {
    val file = File(filePath)
    var lines by remember { mutableStateOf(if (file.exists()) file.readLines() else listOf("")) }
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val highlighter = remember { SmlSyntaxHighlighter(style.colors) }
    val textMeasurer = rememberTextMeasurer()

    // Auto-save after any change
    fun saveWithBackup(content: List<String>) {
        val dir = file.parentFile ?: return
        val baseName = file.nameWithoutExtension
        val ext = file.extension
        val versionedFile = File(dir, "$baseName.${System.currentTimeMillis()}.$ext")
        if (file.exists()) {
            file.copyTo(versionedFile)
        }
        file.writeText(content.joinToString("\n"))
    }

    Box(
        modifier = modifier
            .horizontalScroll(horizontalScroll)
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    // TODO: Cursor placement
                }
            }
        ) {
            highlighter.reset()
            var yOffset = 20f - verticalScroll.value
            for ((index, line) in lines.withIndex()) {
                val tokens = highlighter.highlightLine(line)
                var xOffset = 60f - horizontalScroll.value
                for (token in tokens) {
                    val layoutResult = textMeasurer.measure(
                        text = buildAnnotatedString { append(token.text) },
                        style = TextStyle(
                            color = token.color,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    drawIntoCanvas { canvas ->
                        canvas.save()
                        canvas.translate(xOffset, yOffset)
                        layoutResult.multiParagraph.paint(canvas)
                        canvas.restore()
                    }
                    xOffset += layoutResult.size.width
                }
                yOffset += 20f
            }
        }
    }
}