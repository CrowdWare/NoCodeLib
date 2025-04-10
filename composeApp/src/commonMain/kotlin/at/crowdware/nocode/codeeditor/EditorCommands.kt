package at.crowdware.nocode.codeeditor

import androidx.compose.runtime.snapshots.SnapshotStateList

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
    private val lineIndex: Int,
    private val columnIndex: Int,
    private val text: String
) : EditorCommand {

    override val cursorLineBefore = lineIndex
    override val cursorColumnBefore = columnIndex
    override val cursorLineAfter = lineIndex
    override val cursorColumnAfter = columnIndex + text.length

    override fun execute() {
        editorState.insertText(lineIndex, columnIndex, text)
    }

    override fun undo() {
        editorState.deleteText(lineIndex, columnIndex, text.length)
    }
}

class SplitLineCommand(
    private val editorState: EditorState,
    private val lineIndex: Int,
    private val columnIndex: Int
) : EditorCommand {
    private var remainder = ""

    override val cursorLineBefore = lineIndex
    override val cursorColumnBefore = columnIndex
    override var cursorLineAfter = lineIndex + 1
    override var cursorColumnAfter = 0

    override fun execute() {
        val line = editorState.lines[lineIndex]
        val safeColumn = columnIndex.coerceAtMost(line.length)
        val before = line.substring(0, safeColumn)
        remainder = line.substring(safeColumn)
        editorState.lines[lineIndex] = before
        editorState.lines.add(lineIndex + 1, remainder)

        // cursor position after actual effect
        cursorLineAfter = lineIndex + 1
        cursorColumnAfter = 0
    }

    override fun undo() {
        editorState.lines[lineIndex] += remainder
        editorState.lines.removeAt(lineIndex + 1)
    }
}

class EditorState(
    var lines: SnapshotStateList<String>
) {
    fun insertText(lineIndex: Int, columnIndex: Int, text: String) {
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            lines[lineIndex] =
                line.substring(0, columnIndex) + text + line.substring(columnIndex)
        }
    }

    fun deleteText(lineIndex: Int, columnIndex: Int, length: Int) {
        if (lineIndex in lines.indices) {
            val line = lines[lineIndex]
            val end = (columnIndex + length).coerceAtMost(line.length)
            lines[lineIndex] =
                line.removeRange(columnIndex, end)
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