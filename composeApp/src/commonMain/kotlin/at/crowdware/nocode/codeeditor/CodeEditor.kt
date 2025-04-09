package at.crowdware.nocode.codeeditor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var cursorLine by remember { mutableStateOf(0) }
    var cursorColumn by remember { mutableStateOf(0) }

    val lineHeight = 25f
    val yOffsetStart = 8f
    val canvasHeight = (lines.size * lineHeight).toInt()
    val canvasWidth = 2000
    val fontSize = 14.sp
    val fontFamily = FontFamily.Monospace

    var isCursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(cursorLine, cursorColumn) {
        while (true) {
            isCursorVisible = !isCursorVisible
            kotlinx.coroutines.delay(500)
        }
    }

    fun measureTextWidth(text: String, upToColumn: Int): Float {
        val textToMeasure = text.take(upToColumn.coerceAtMost(text.length))
        val result = textMeasurer.measure(
            text = buildAnnotatedString { append(textToMeasure) },
            style = TextStyle(
                fontSize = fontSize,
                fontFamily = fontFamily
            )
        )
        return result.size.width.toFloat()
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when (event.key) {
                        Key.DirectionLeft -> {
                            if (cursorColumn > 0) {
                                cursorColumn--
                            } else if (cursorLine > 0) {
                                cursorLine--
                                cursorColumn = lines.getOrNull(cursorLine)?.length ?: 0
                            }
                            true
                        }
                        Key.DirectionRight -> {
                            val line = lines.getOrNull(cursorLine) ?: ""
                            if (cursorColumn < line.length) {
                                cursorColumn++
                            } else if (cursorLine < lines.lastIndex) {
                                cursorLine++
                                cursorColumn = 0
                            }
                            true
                        }
                        Key.DirectionUp -> {
                            if (cursorLine > 0) {
                                cursorLine--
                                val line = lines.getOrNull(cursorLine) ?: ""
                                cursorColumn = minOf(cursorColumn, line.length)
                            }
                            true
                        }
                        Key.DirectionDown -> {
                            if (cursorLine < lines.lastIndex) {
                                cursorLine++
                                val line = lines.getOrNull(cursorLine) ?: ""
                                cursorColumn = minOf(cursorColumn, line.length)
                            }
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp, bottom = 12.dp)
                    .verticalScroll(verticalScroll)
                    .horizontalScroll(horizontalScroll)
            ) {
                Canvas(
                    modifier = Modifier
                        .size(canvasWidth.dp, canvasHeight.dp)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                cursorLine = ((offset.y + verticalScroll.value - yOffsetStart) / lineHeight).toInt()
                                val lineText = lines.getOrNull(cursorLine) ?: ""
                                val position = (offset.x + horizontalScroll.value - 60f).toInt()
                                var found = false
                                for (i in 1..lineText.length) {
                                    if (measureTextWidth(lineText, i) > position) {
                                        cursorColumn = i - 1
                                        found = true
                                        break
                                    }
                                }
                                if (!found)
                                    cursorColumn = lineText.length
                            }
                        }
                ) {
                    var inString = false
                    var yOffset = yOffsetStart - verticalScroll.value
                    for ((index, line) in lines.withIndex()) {
                        val (tokens, nextInString) = highlighter.highlightLine(line, inString)
                        inString = nextInString

                        var xOffset = 60f
                        for (token in tokens) {
                            val layoutResult = textMeasurer.measure(
                                text = buildAnnotatedString { append(token.text) },
                                style = TextStyle(
                                    color = token.color,
                                    fontSize = fontSize,
                                    fontFamily = fontFamily
                                )
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = buildAnnotatedString { append(token.text) },
                                topLeft = Offset(xOffset, yOffset),
                                style = TextStyle(
                                    color = token.color,
                                    fontSize = fontSize,
                                    fontFamily = fontFamily
                                )
                            )
                            xOffset += layoutResult.size.width
                        }

                        if (index == cursorLine && isCursorVisible) {
                            val lineText = lines.getOrNull(cursorLine) ?: ""
                            val cursorX = 60f + measureTextWidth(lineText, cursorColumn) - horizontalScroll.value
                            drawLine(
                                color = style.cursorColor,
                                start = Offset(cursorX, yOffset),
                                end = Offset(cursorX, yOffset + lineHeight - 5),
                                strokeWidth = 1f
                            )
                        }

                        yOffset += lineHeight
                    }
                }
            }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState = verticalScroll),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
        HorizontalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState = horizontalScroll),
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
        )
    }
}