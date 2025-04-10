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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


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
    val cursorPosition = remember { CursorPosition(0, 0) }
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

    var isCursorVisible by remember { mutableStateOf(true) }
    /*var blinkJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    fun restartCursorBlink(scope: CoroutineScope) {
        isCursorVisible = true
        blinkJob?.cancel()
        blinkJob = scope.launch {
            delay(1000)
            while (true) {
                isCursorVisible = false
                delay(300)
                isCursorVisible = true
                delay(300)
            }
        }
    }
    LaunchedEffect(Unit) {
        restartCursorBlink(coroutineScope)
    }*/


    LaunchedEffect(cursorPosition) {
        while (true) {
            isCursorVisible = !isCursorVisible
            delay(300)
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
                            if (cursorPosition.column > 0) {
                                cursorPosition.column--
                            } else if (cursorPosition.line > 0) {
                                cursorPosition.line--
                                cursorPosition.column = editorState.lines.getOrNull(cursorPosition.line)?.length ?: 0
                            }
                            //restartCursorBlink(coroutineScope)
                            true
                        }
                        event.key == Key.DirectionRight -> {
                            val line = editorState.lines.getOrNull(cursorPosition.line) ?: ""
                            if (cursorPosition.column < line.length) {
                                cursorPosition.column++
                            } else if (cursorPosition.line < editorState.lines.lastIndex) {
                                cursorPosition.line++
                                cursorPosition.column = 0
                            }
                            //restartCursorBlink(coroutineScope)
                            true
                        }
                        event.key == Key.DirectionUp -> {
                            if (cursorPosition.line > 0) {
                                cursorPosition.line--
                                val line = editorState.lines.getOrNull(cursorPosition.line) ?: ""
                                cursorPosition.column = minOf(cursorPosition.column, line.length)
                            }
                            //restartCursorBlink(coroutineScope)
                            true
                        }
                        event.key == Key.DirectionDown -> {
                            if (cursorPosition.line < editorState.lines.lastIndex) {
                                cursorPosition.line++
                                val line = editorState.lines.getOrNull(cursorPosition.line) ?: ""
                                cursorPosition.column = minOf(cursorPosition.column, line.length)
                            }
                            //restartCursorBlink(coroutineScope)
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
                                cursorPosition.line = ((offset.y + verticalScroll.value - yOffsetStart) / lineHeight).toInt()
                                val lineText = editorState.lines.getOrNull(cursorPosition.line) ?: ""
                                val position = (offset.x + horizontalScroll.value - 60f).toInt()
                                var found = false
                                for (i in 1..lineText.length) {
                                    if (measureTextWidth(lineText, i) > position) {
                                        cursorPosition.column = i - 1
                                        found = true
                                        break
                                    }
                                }
                                if (!found)
                                    cursorPosition.column = lineText.length
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