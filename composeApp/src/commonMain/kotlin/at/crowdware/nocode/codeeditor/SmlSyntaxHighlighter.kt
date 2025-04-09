package at.crowdware.nocode.codeeditor

import androidx.compose.ui.graphics.Color
import at.crowdware.nocode.theme.ExtendedColors

data class TextToken(val text: String, val color: Color)

class SmlSyntaxHighlighter(private val colors: ExtendedColors) {

    fun highlightLine(line: String, inStringStart: Boolean): Pair<List<TextToken>, Boolean> {
        val tokens = mutableListOf<TextToken>()
        var i = 0
        var buffer = ""
        var inString = inStringStart

        while (i < line.length) {
            val c = line[i]

            if (!inString && i + 1 < line.length && line.substring(i, i + 2) == "//") {
                if (buffer.isNotEmpty()) tokens += TextToken(buffer, colors.defaultTextColor)
                tokens += TextToken(line.substring(i), colors.commentColor)
                return Pair(tokens, false)
            }

            if (c == '"' && (i == 0 || line[i - 1] != '\\')) {
                if (inString) {
                    buffer += c
                    tokens += TextToken(buffer, colors.attributeValueColor)
                    buffer = ""
                    inString = false
                } else {
                    if (buffer.isNotEmpty()) tokens += TextToken(buffer, colors.defaultTextColor)
                    buffer = "" + c
                    inString = true
                }
            } else {
                buffer += c
            }

            i++
        }

        if (buffer.isNotEmpty()) {
            tokens += TextToken(buffer, if (inString) colors.attributeValueColor else colors.defaultTextColor)
        }

        return Pair(tokens, inString)
    }
}