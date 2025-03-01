/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import at.crowdware.nocodelib.theme.ExtendedColors
import at.crowdware.nocodelib.theme.ExtendedTheme
import at.crowdware.nocodelib.utils.uiStates
import at.crowdware.nocodelib.viewmodel.GlobalProjectState


@Composable
fun SyntaxTextField(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    extension: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentState = uiStates.current
    val extendedColors = ExtendedTheme.colors
    var pos by remember { mutableStateOf(Offset(0f,0f)) }
    val isFocused by remember { mutableStateOf(false) }
    val isHovered = currentState.hasCollided.value
    val currentProject = GlobalProjectState.projectState
    val backgroundColor = MaterialTheme.colors.surface
    val cursorColor = MaterialTheme.colors.onSurface
    val focusedBorderColor = MaterialTheme.colors.primary
    val unfocusedBorderColor = MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
    val borderColor = when {
        isHovered -> MaterialTheme.colors.secondary
        isFocused -> focusedBorderColor
        else -> unfocusedBorderColor
    }

    CustomSelectionColors {
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .border(BorderStroke(1.dp, borderColor))
                .padding(start = 6.dp, top = 4.dp)
                .onGloballyPositioned {
                    pos = it.localToWindow(Offset(0f,0f))
                    currentState.targetLocalPosition = it.localToWindow(Offset(0f,0f))
                    currentState.targetSize = it.size.toSize()
                }
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState)
                    .padding(6.dp)
            ) {
                if (currentProject != null) {
                    if (currentProject.fileName.length > 0) {
                        key(currentProject.fileName) {
                            BasicTextField(
                                value = textFieldValue,
                                onValueChange = onValueChange,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .heightIn(min = 640.dp)
                                    .background(Color.Transparent)
                                    .onKeyEvent { keyEvent ->
                                        when (keyEvent.key) {
                                            Key.Tab -> {
                                                if (keyEvent.type == KeyEventType.KeyUp) {
                                                    val currentText = textFieldValue.text
                                                    val selection = textFieldValue.selection

                                                    // Clean the text by removing tabs and maintain logical cursor alignment
                                                    val beforeCursor = currentText.substring(0, selection.start).replace("\t", "")
                                                    val afterCursor = currentText.substring(selection.start).replace("\t", "")
                                                    val cleanedText = beforeCursor + afterCursor
                                                    val adjustedCursorPosition = beforeCursor.length

                                                    // Insert 4 spaces at the adjusted cursor position
                                                    val updatedText = StringBuilder(cleanedText).apply {
                                                        insert(adjustedCursorPosition, "    ")
                                                    }.toString()

                                                    // Update the text field with the cleaned and modified text
                                                    onValueChange(
                                                        textFieldValue.copy(
                                                            text = updatedText,
                                                            selection = TextRange(adjustedCursorPosition + 4) // Place cursor after inserted spaces
                                                        )
                                                    )
                                                    return@onKeyEvent true
                                                }
                                                false
                                            }
                                            else -> false
                                        }
                                    },
                                    // TODO: handle backspace to delete 4 blanks to simulated tab delete
                                    textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colors.onSurface,
                                    fontFamily = FontFamily.Monospace
                                ),
                                cursorBrush = SolidColor(cursorColor),
                                visualTransformation = when (extension) {
                                    "sml" -> SmlSyntaxHighlighter(extendedColors)
                                    "md" -> MarkdownSyntaxHighlighter(extendedColors)
                                    else -> VisualTransformation.None
                                },
                                maxLines = Int.MAX_VALUE
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(12.dp)
                    .padding(vertical = 4.dp),
                adapter = rememberScrollbarAdapter(scrollState)
            )
        }
    }
}

class SmlSyntaxHighlighter(val colors: ExtendedColors) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val builder = AnnotatedString.Builder(text)

        // Highlight SML elements
        val elementRegex = Regex("(\\w+)\\s*\\{")
        elementRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.syntaxColor), match.range.first, match.range.last)
            builder.addStyle(SpanStyle(color = colors.bracketColor), match.range.last, match.range.last + 1)
        }

        // Highlight string values first (including those with colons)
        val stringRegex = Regex("\"[^\"]*\"")
        stringRegex.findAll(text).forEach { match ->
            builder.addStyle(SpanStyle(color = colors.attributeValueColor), match.range.first, match.range.last + 1)
        }

        // Highlight properties (excluding the colon)
        val propertyRegex = Regex("(\\w+)(?=\\s*:)")
        propertyRegex.findAll(text).forEach { match ->
            // Check if this property name is not within a string
            if (!isWithinString(text, match.range.first)) {
                builder.addStyle(SpanStyle(color = colors.attributeNameColor), match.range.first, match.range.last + 1)
            }
        }

        // Highlight colons and closing brackets
        val colonAndBracketRegex = Regex(":|[}]")
        colonAndBracketRegex.findAll(text).forEach { match ->
            // Check if this colon or bracket is not within a string
            if (!isWithinString(text, match.range.first)) {
                builder.addStyle(SpanStyle(color = colors.bracketColor), match.range.first, match.range.last + 1)
            }
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun isWithinString(text: CharSequence, index: Int): Boolean {
        var inString = false
        for (i in 0 until index) {
            if (text[i] == '"') {
                inString = !inString
            }
        }
        return inString
    }
}

@Composable
fun CustomSelectionColors(content: @Composable () -> Unit) {
    val customSelectionColors = TextSelectionColors(
        handleColor = Color.Magenta,
        backgroundColor = Color.LightGray.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customSelectionColors) {
        content()
    }
}

class MarkdownSyntaxHighlighter(val colors: ExtendedColors) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(applyMarkdownHighlighting(text), OffsetMapping.Identity)
    }

    private fun applyMarkdownHighlighting(text: AnnotatedString): AnnotatedString {
        val builder = AnnotatedString.Builder(text)

        // Apply all the highlight rules
        highlightHeaders(builder, text)
        highlightBold(builder, text)
        highlightItalic(builder, text)
        highlightLinks(builder, text)
        highlightCodeBlocks(builder, text)
        highlightListItems(builder, text)
        highlightInlineHtml(builder, text)
        return builder.toAnnotatedString()
    }

    // Header highlighting: "# Title" and "## Subtitle"
    private fun highlightHeaders(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val headerRegex = Regex("(^#+)\\s(.+)", RegexOption.MULTILINE)
        headerRegex.findAll(text.text).forEach { match ->
            // Berechnung des Bereichs für headerMarks
            val headerMarks = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val headerMarksStart = matchStart + match.value.indexOf(headerMarks)
            val headerMarksEnd = headerMarksStart + headerMarks.length
            val headerMarksRange = headerMarksStart until headerMarksEnd

            // Berechnung des Bereichs für headerText
            val headerText = match.groups[2]?.value ?: ""
            val headerTextStart = matchStart + match.value.indexOf(headerText)
            val headerTextEnd = headerTextStart + headerText.length
            val headerTextRange = headerTextStart until headerTextEnd

            // "#" in syntax color
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = headerMarksRange.first,
                end = headerMarksRange.last + 1
            )
            // Text in magenta
            builder.addStyle(
                style = SpanStyle(color = colors.mdHeader),
                start = headerTextRange.first,
                end = headerTextRange.last + 1
            )
        }
    }

    // Bold highlighting: "**bold**"
    private fun highlightBold(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        boldRegex.findAll(text.text).forEach { match ->
            val boldText = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val boldTextStart = matchStart + match.value.indexOf(boldText)
            val boldTextEnd = boldTextStart + boldText.length
            val boldTextRange = boldTextStart until boldTextEnd

            // "**" in syntax color (white)
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.first + 2
            )
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.last - 1,
                end = match.range.last + 1
            )

            // Bold text in default text color
            builder.addStyle(
                style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.defaultTextColor),
                start = boldTextRange.first,
                end = boldTextRange.last + 1
            )
        }
    }

    // Italic highlighting: "*italic*"
    private fun highlightItalic(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val italicRegex = Regex("\\*(.*?)\\*")
        italicRegex.findAll(text.text).forEach { match ->
            val italicText = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val italicTextStart = matchStart + match.value.indexOf(italicText)
            val italicTextEnd = italicTextStart + italicText.length
            val italicTextRange = italicTextStart until italicTextEnd

            // "*" in syntax color (white)
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.first + 1
            )
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.last,
                end = match.range.last + 1
            )

            // Italic text in default text color
            builder.addStyle(
                style = SpanStyle(fontStyle = FontStyle.Italic, color = colors.defaultTextColor),
                start = italicTextRange.first,
                end = italicTextRange.last + 1
            )
        }
    }

    // Link highlighting: "[link](url)"
    private fun highlightLinks(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val linkRegex = Regex("\\[(.+?)\\]\\((.+?)\\)")
        linkRegex.findAll(text.text).forEach { match ->
            val linkText = match.groups[1]?.value ?: ""
            var matchStart = match.range.first
            val linkTextStart = matchStart + match.value.indexOf(linkText)
            val linkTextEnd = linkTextStart + linkText.length
            val linkTextRange = linkTextStart until linkTextEnd

            val urlText = match.groups[2]?.value ?: ""
            val urlTextStart = matchStart + match.value.indexOf(urlText)
            val urlTextEnd = urlTextStart + urlText.length
            val urlRange = urlTextStart until urlTextEnd

            // "[" and "]" in blue (covering the entire link text with brackets)
            builder.addStyle(
                style = SpanStyle(color = colors.linkColor),
                start = match.range.first, // The '['
                end = linkTextRange.last + 2 // The ']'
            )

            // "(" and ")" in green
            builder.addStyle(
                style = SpanStyle(color = colors.attributeValueColor),
                start = linkTextRange.last + 2, // The '('
                end = urlRange.last + 2 // The ')' (include the final parenthesis)
            )

            // URL in blue
            builder.addStyle(
                style = SpanStyle(color = colors.linkColor),
                start = urlRange.first,
                end = urlRange.last + 1
            )
        }
    }

    private fun highlightCodeBlocks(builder: AnnotatedString.Builder, text: AnnotatedString) {
        //val codeBlockRegex = Regex("```(.*?)```", RegexOption.DOT_MATCHES_ALL)
        val codeBlockRegex = at.crowdware.nocodelib.ui.createCodeBlockRegex()
        codeBlockRegex.findAll(text.text).forEach { match ->
            // Render the code block in a different color (e.g., light gray for background, dark gray for text)
            builder.addStyle(
                style = SpanStyle(/*background = Color.LightGray,*/ color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.last + 1
            )
        }
    }

    // List item highlighting: "- Item"
    private fun highlightListItems(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val listItemRegex = Regex("^(-)\\s", RegexOption.MULTILINE)
        listItemRegex.findAll(text.text).forEach { match ->
            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor),
                start = match.range.first,
                end = match.range.last
            )
        }
    }

    private fun highlightInlineHtml(builder: AnnotatedString.Builder, text: AnnotatedString) {
        val htmlTagRegex = Regex("<([a-zA-Z]+)(\\s+[a-zA-Z]+=\"[^\"]*\")*\\s*/?>")
        htmlTagRegex.findAll(text.text).forEach { match ->

            val tagName = match.groups[1]?.value ?: ""
            val matchStart = match.range.first
            val tagNameStart = matchStart + match.value.indexOf(tagName)
            val tagNameEnd = tagNameStart + tagName.length
            val tagNameRange = tagNameStart until tagNameEnd
            val attributesRegex = Regex("([a-zA-Z]+)=(\"[^\"]*\")")
            val attributesMatch = attributesRegex.findAll(match.value)

            builder.addStyle(
                style = SpanStyle(color = colors.syntaxColor), // Purple for HTML tag
                start = match.range.first,
                end = tagNameRange.last + 1
            )

            // HTML attributes in attributeNameColor and values in attributeValueColor
            attributesMatch.forEach { attrMatch ->
                // Berechnung des Bereichs für attrName
                val attrName = attrMatch.groups[1]?.value ?: ""
                val attrMatchStart = attrMatch.range.first
                val attrNameStart = attrMatchStart + attrMatch.value.indexOf(attrName)
                val attrNameEnd = attrNameStart + attrName.length
                val attrNameRange = attrNameStart until attrNameEnd

                // Berechnung des Bereichs für attrValue
                val attrValue = attrMatch.groups[2]?.value ?: ""
                val attrValueStart = attrMatchStart + attrMatch.value.indexOf(attrValue)
                val attrValueEnd = attrValueStart + attrValue.length
                val attrValueRange = attrValueStart until attrValueEnd

                // Attribute name in attributeNameColor (e.g., src, id)
                builder.addStyle(
                    style = SpanStyle(color = colors.attributeNameColor),
                    start = match.range.first + attrNameRange.first,
                    end = match.range.first + attrNameRange.last + 1
                )

                // `=` and value in attributeValueColor (e.g., ="link")
                builder.addStyle(
                    style = SpanStyle(color = colors.attributeValueColor),
                    start = match.range.first + attrValueRange.first - 1, // Include `=`
                    end = match.range.first + attrValueRange.last + 1 // Include `"`
                )
            }

            // Close tag `/>` in purple
            val closeTagIndex = match.range.last - 1
            if (text.text[closeTagIndex] == '/') {
                builder.addStyle(
                    style = SpanStyle(color = colors.syntaxColor),
                    start = closeTagIndex,
                    end = match.range.last + 1 // Cover `/>`
                )
            } else {
                builder.addStyle(
                    style = SpanStyle(color = colors.syntaxColor),
                    start = match.range.last,
                    end = match.range.last + 1
                )
            }
        }
    }
}

