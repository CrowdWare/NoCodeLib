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
    override val cursorLineAfter = lineIndex + 1
    override val cursorColumnAfter = 0

    override fun execute() {
        val line = editorState.lines[lineIndex]
        val safeColumn = columnIndex.coerceAtMost(line.length)
        val before = line.substring(0, safeColumn)
        remainder = line.substring(safeColumn)
        editorState.lines[lineIndex] = before
        editorState.lines.add(lineIndex + 1, remainder)
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

class CommandManager {
    private val undoStack = mutableListOf<EditorCommand>()
    private val redoStack = mutableListOf<EditorCommand>()

    var cursorLine: Int = 0
        private set
    var cursorColumn: Int = 0
        private set

    fun executeCommand(cmd: EditorCommand) {
        cmd.execute()
        undoStack.add(cmd)
        redoStack.clear()
        cursorLine = cmd.cursorLineAfter
        cursorColumn = cmd.cursorColumnAfter
    }

    fun undo() {
        undoStack.removeLastOrNull()?.let {
            it.undo()
            redoStack.add(it)
            cursorLine = it.cursorLineBefore
            cursorColumn = it.cursorColumnBefore
        }
    }

    fun redo() {
        redoStack.removeLastOrNull()?.let {
            it.execute()
            undoStack.add(it)
            cursorLine = it.cursorLineAfter
            cursorColumn = it.cursorColumnAfter
        }
    }
}