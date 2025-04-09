package at.crowdware.nocode.codeeditor

import androidx.compose.ui.graphics.Color
import at.crowdware.nocode.theme.ExtendedColors

data class TextToken(val text: String, val color: Color)

class SmlSyntaxHighlighter(val colors: ExtendedColors) {
    var inString = false

    fun reset() {
        inString = false
    }

    fun highlightLine(line: String): List<TextToken> {
        val tokens = mutableListOf<TextToken>()
        var buffer = ""
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '"' && (i == 0 || line[i - 1] != '\\')) {
                inString = !inString
                buffer += c
                tokens += TextToken(buffer, colors.attributeValueColor)
                buffer = ""
            } else if (!inString && i + 1 < line.length && line.substring(i, i + 2) == "//") {
                tokens += TextToken(line.substring(i), colors.commentColor)
                break
            } else {
                buffer += c
            }
            i++
        }
        if (buffer.isNotEmpty()) {
            tokens += TextToken(buffer, if (inString) colors.attributeValueColor else colors.defaultTextColor)
        }
        return tokens
    }
}