package at.crowdware.nocode.codeeditor

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.input.key.Key
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EditorCommand {
    fun execute()
    fun undo()
}

data class CursorPosition(var line: Int, var column: Int)

class MoveCursorCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition,
    private val direction: Key,
) : EditorCommand {
    private val cursorLine = cursor.line
    private val cursorColumn = cursor.column

    override fun execute() {
        when(direction) {
            Key.DirectionRight -> {
                val line = editorState.lines.getOrNull(cursor.line) ?: ""
                if (cursor.column < line.length) {
                    cursor.column++
                } else if (cursor.line < editorState.lines.lastIndex) {
                    cursor.line++
                    cursor.column = 0
                }
            }
            Key.DirectionLeft -> {
                if (cursor.column > 0) {
                    cursor.column--
                } else if (cursor.line > 0) {
                    cursor.line--
                    cursor.column = editorState.lines.getOrNull(cursor.line)?.length ?: 0
                }
            }
            Key.DirectionDown -> {
                if (cursor.line < editorState.lines.lastIndex) {
                    cursor.line++
                    val line = editorState.lines.getOrNull(cursor.line) ?: ""
                    cursor.column = minOf(cursor.column, line.length)
                }
            }
            Key.DirectionUp -> {
                if (cursor.line > 0) {
                    cursor.line--
                    cursor.column = minOf(cursor.column, editorState.lines.getOrNull(cursor.line)?.length ?: 0)
                }
            }
        }
    }

    override fun undo() {
        cursor.line = cursorLine
        cursor.column = cursorColumn
    }
}

class InsertTextCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition,
    private val text: String
) : EditorCommand {
    val cursorColumn = cursor.column

    override fun execute() {
        editorState.insertText(cursor.line, cursor.column, text)
        editorState.updateTextFlow()
        cursor.column += text.length
    }

    override fun undo() {
        val startColumn = cursor.column - text.length
        editorState.deleteText(cursor.line, startColumn, text.length)
        editorState.updateTextFlow()
        cursor.column = cursorColumn
    }
}

class SplitLineCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition
) : EditorCommand {
    private var remainder = ""
    private var before = ""
    private val cursorLine = cursor.line
    private val cursorColumn = cursor.column

    override fun execute() {
        val line = editorState.lines[cursor.line]
        before = line.substring(0, cursor.column)
        remainder = line.substring(cursor.column)
        editorState.lines[cursorLine] = before
        editorState.lines.add(cursorLine + 1, remainder)
        cursor.line = cursorLine + 1
        cursor.column = 0
        editorState.updateTextFlow()
    }

    override fun undo() {
        editorState.lines[cursorLine] = before + remainder
        editorState.lines.removeAt(cursorLine + 1)
        cursor.column = cursorColumn
        cursor.line = cursorLine
        editorState.updateTextFlow()
    }
}

class BackspaceCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition
) : EditorCommand {
    private var merged = false
    private var changed = false
    private var originalLine: String = ""
    private var prevLine: String = ""
    private val cursorLine = cursor.line
    private val cursorColumn = cursor.column

    override fun execute() {
        if (cursor.column > 0) {
            val line = editorState.lines[cursor.line]
            originalLine = line
            editorState.lines[cursor.line] = line.removeRange( cursor.column - 1,  cursor.column)
            cursor.line = cursorLine
            cursor.column -= 1
            changed = true
            editorState.updateTextFlow()
        } else if (cursor.line > 0) {
            originalLine = editorState.lines.removeAt(cursor.line)
            prevLine = editorState.lines[cursor.line - 1]
            editorState.lines[cursor.line - 1] = prevLine + originalLine
            merged = true
            changed = true
            cursor.line -= 1
            cursor.column = prevLine.length
            editorState.updateTextFlow()
        }
    }

    override fun undo() {
        if (!changed)
            return
        if (merged) {
            editorState.lines[cursorLine-1] = prevLine
            editorState.lines.add(cursorLine, originalLine)
        } else {
            editorState.lines[cursorLine] = originalLine
        }
        cursor.line = cursorLine
        cursor.column = cursorColumn
        editorState.updateTextFlow()
    }
}

class CommandManager(private val cursor: CursorPosition) {
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()

    fun executeCommand(cmd: EditorCommand) {
        cmd.execute()
        undoStack.add(cmd)
        redoStack.clear()
    }

    fun undo() {
        undoStack.removeLastOrNull()?.let {
            it.undo()
            redoStack.add(it)
        }
    }

    fun redo() {
        redoStack.removeLastOrNull()?.let {
            it.execute()
            undoStack.add(it)
        }
    }
}

class EditorState(
    var lines: SnapshotStateList<String>
) {
    private val _textFlow = MutableStateFlow(lines.joinToString("\n"))
    val textFlow: StateFlow<String> = _textFlow.asStateFlow()

    fun updateTextFlow() {
        _textFlow.value = lines.joinToString("\n")
    }

    fun insertText(lineIndex: Int, columnIndex: Int, text: String) {
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] =
                line.substring(0, columnIndex) + text + line.substring(columnIndex)
            updateTextFlow()
        }
    }

    fun deleteText(lineIndex: Int, columnIndex: Int, length: Int) {
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            val end = (columnIndex + length).coerceAtMost(line.length)
            lines[lineIndex] =
                line.removeRange(columnIndex, end)
            updateTextFlow()
        }
    }
}