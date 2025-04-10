package at.crowdware.nocode.codeeditor

import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface EditorCommand {
    val cursorLineBefore: Int
    val cursorColumnBefore: Int
    val cursorLineAfter: Int
    val cursorColumnAfter: Int

    fun execute()
    fun undo()
}

data class CursorPosition(var line: Int, var column: Int)

class InsertTextCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition,
    private val text: String
) : EditorCommand {

    override val cursorLineBefore = cursor.line
    override val cursorColumnBefore = cursor.column
    override val cursorLineAfter = cursor.line
    override val cursorColumnAfter = cursor.column + text.length

    override fun execute() {
        editorState.insertText(cursor.line, cursor.column, text)
        editorState.updateTextFlow()
    }

    override fun undo() {
        editorState.deleteText(cursor.line, cursor.column, text.length)
        editorState.updateTextFlow()
    }
}

class SplitLineCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition
) : EditorCommand {
    private var remainder = ""
    private var before = ""
    private val index = cursor.line

    override val cursorLineBefore = cursor.line
    override val cursorColumnBefore = cursor.column
    override var cursorLineAfter = cursor.line + 1
    override var cursorColumnAfter = 0

    override fun execute() {
        val line = editorState.lines[index]
        val safeColumn = cursor.column.coerceAtMost(line.length)
        before = line.substring(0, safeColumn)
        remainder = line.substring(safeColumn)
        editorState.lines[index] = before
        editorState.lines.add(index + 1, remainder)

        cursorLineAfter = index + 1
        cursorColumnAfter = 0
        editorState.updateTextFlow()
    }

    override fun undo() {
        editorState.lines[index] = before + remainder
        editorState.lines.removeAt(index + 1)
        editorState.updateTextFlow()
    }
}

class BackspaceCommand(
    private val editorState: EditorState,
    private val cursor: CursorPosition
) : EditorCommand {

    private var deletedChar: String = ""
    private var mergedLine: String = ""
    private var removedLine: String = ""
    private var merged = false

    override val cursorLineBefore = cursor.line
    override val cursorColumnBefore = cursor.column

    override var cursorLineAfter = cursor.line
    override var cursorColumnAfter = cursor.column

    override fun execute() {
        if (cursor.column > 0) {
            val line = editorState.lines[cursor.line]
            val col = cursor.column
            deletedChar = line[col - 1].toString()
            editorState.lines[cursor.line] = line.removeRange(col - 1, col)
            cursorLineAfter = cursor.line
            cursorColumnAfter = col - 1
            editorState.updateTextFlow()
        } else if (cursor.line > 0) {
            // Merge with previous line
            val currentLine = editorState.lines.removeAt(cursor.line)
            val prevLineIndex = cursor.line - 1
            val prevLine = editorState.lines[prevLineIndex]
            editorState.lines[prevLineIndex] = prevLine + currentLine
            removedLine = currentLine
            mergedLine = prevLine
            merged = true
            cursorLineAfter = prevLineIndex
            cursorColumnAfter = mergedLine.length
            editorState.updateTextFlow()
        }
    }

    override fun undo() {
        if (merged) {
            val prevLineIndex = cursorLineAfter
            editorState.lines[prevLineIndex] = mergedLine
            editorState.lines.add(prevLineIndex + 1, removedLine)
            editorState.updateTextFlow()
        } else {
            val line = editorState.lines[cursorLineAfter]
            editorState.lines[cursorLineAfter] =
                line.substring(0, cursorColumnAfter) + deletedChar + line.substring(cursorColumnAfter)
            editorState.updateTextFlow()
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

class CommandManager(private val cursor: CursorPosition) {
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()

    fun executeCommand(cmd: EditorCommand) {
        cmd.execute()
        undoStack.add(cmd)
        redoStack.clear()
        cursor.line = cmd.cursorLineAfter
        cursor.column = cmd.cursorColumnAfter
    }

    fun undo() {
        undoStack.removeLastOrNull()?.let {
            it.undo()
            redoStack.add(it)
            cursor.line = it.cursorLineBefore
            cursor.column = it.cursorColumnBefore
        }
    }

    fun redo() {
        redoStack.removeLastOrNull()?.let {
            it.execute()
            undoStack.add(it)
            cursor.line = it.cursorLineAfter
            cursor.column = it.cursorColumnAfter
        }
    }
}