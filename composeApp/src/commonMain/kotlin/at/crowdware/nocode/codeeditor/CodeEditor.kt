package at.crowdware.nocode.codeeditor

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.io.File

// Was fehlt noch?
// Markieren copy, paste, del
// safe
// Syntax Color


@Composable
fun CodeEditor(
    modifier: Modifier = Modifier,
    filePath: String,
    style: CodeEditorStyle
) {
    val file = File(filePath)
    val editorState = remember {
        val initialLines = if (file.exists()) file.readLines().toMutableStateList() else mutableStateListOf("")
        EditorState(initialLines)
    }
    val cursorPosition by remember { mutableStateOf(CursorPosition(0, 0)) }
    val commandManager = remember { CommandManager(cursorPosition) }

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val highlighter = remember { SmlSyntaxHighlighter(style.colors) }
    val textMeasurer = rememberTextMeasurer()

    val lineHeight = 25f
    val yOffsetStart = 8f
    val canvasHeight by remember(editorState.lines) {
        derivedStateOf { (editorState.lines.size * lineHeight).toInt() }
    }

    val canvasWidth = 2000
    val fontSize = 14.sp
    val fontFamily = FontFamily.Monospace
    val lastNavigationTime by remember { mutableStateOf(0L) }
    var isCursorVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            val elapsed = System.currentTimeMillis() - lastNavigationTime
            if (elapsed < 300) {
                isCursorVisible = true
                delay(50)
            } else {
                isCursorVisible = !isCursorVisible
                delay(300)
            }
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
                    when {
                        event.key == Key.Enter -> {
                            val cmd = SplitLineCommand(editorState, cursorPosition)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.Backspace -> {
                            val cmd = BackspaceCommand(editorState, cursorPosition)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.Tab -> {
                            val cmd = InsertTextCommand(editorState, cursorPosition, "    ")
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.Z && (event.isCtrlPressed || event.isMetaPressed) && !event.isShiftPressed -> {
                            commandManager.undo()
                            true
                        }
                        event.key == Key.Z && (event.isCtrlPressed || event.isMetaPressed) && event.isShiftPressed -> {
                            commandManager.redo()
                            true
                        }
                        event.key == Key.DirectionLeft -> {
                            isCursorVisible = true
                            val cmd = MoveCursorCommand(editorState, cursorPosition, Key.DirectionLeft, commandManager)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.DirectionRight -> {
                            isCursorVisible = true
                            val cmd = MoveCursorCommand(editorState, cursorPosition, Key.DirectionRight, commandManager)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.DirectionUp -> {
                            isCursorVisible = true
                            val cmd = MoveCursorCommand(editorState, cursorPosition, Key.DirectionUp, commandManager)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key == Key.DirectionDown -> {
                            isCursorVisible = true
                            val cmd = MoveCursorCommand(editorState, cursorPosition, Key.DirectionDown, commandManager)
                            commandManager.executeCommand(cmd)
                            true
                        }
                        event.key.nativeKeyCode in 32..126 -> {
                            val char = event.utf16CodePoint.toChar()
                            val cmd = InsertTextCommand(editorState, cursorPosition, char.toString())
                            commandManager.executeCommand(cmd)
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
                                val line = ((offset.y + verticalScroll.value - yOffsetStart) / lineHeight).toInt()
                                var column = -1
                                val lineText = editorState.lines.getOrNull(line) ?: ""
                                val position = (offset.x + horizontalScroll.value - 60f).toInt()
                                var found = false
                                for (i in 1..lineText.length) {
                                    if (measureTextWidth(lineText, i) > position) {
                                        column = i - 1
                                        found = true
                                        break
                                    }
                                }
                                if (!found)
                                    column = lineText.length

                                val cmd = SetCursorPosCommand(cursorPosition, line, column, commandManager)
                                commandManager.executeCommand(cmd)
                            }
                        }
                ) {
                    var inString = false
                    var yOffset = yOffsetStart - verticalScroll.value

                    for ((index, line) in editorState.lines.withIndex()) {
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

                        if (index == cursorPosition.line && isCursorVisible) {
                            val lineText = editorState.lines.getOrNull(cursorPosition.line) ?: ""
                            val cursorX = 60f + measureTextWidth(lineText, cursorPosition.column) - horizontalScroll.value
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
