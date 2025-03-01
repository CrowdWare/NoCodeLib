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

package at.crowdware.nocodelib.view.desktop


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodelib.theme.ExtendedTheme
import at.crowdware.nocodelib.utils.Page
import at.crowdware.nocodelib.utils.UIElement
import at.crowdware.nocodelib.utils.UIElement.*
import at.crowdware.nocodelib.viewmodel.GlobalProjectState
import at.crowdware.nocodelib.viewmodel.ProjectState
import java.io.File

@Composable
fun mobilePreview(currentProject: ProjectState?) {
    var page: Page? = if (currentProject?.isPageLoaded == true) currentProject.page else null
    val scrollState = rememberScrollState()

    if (page == null && currentProject != null) {
        page = currentProject.cachedPage
    }

    Column(modifier = Modifier.width(430.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
        BasicText(
            text = "Mobile Preview",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            // Outer phone box with fixed aspect ratio (9:16) that scales dynamically
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(360.dp, 640.dp)
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF353739))
                    .border(2.dp, Color.Gray, RoundedCornerShape(24.dp))
            ) {
                // Inner screen with relative size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.9f)
                        .align(Alignment.Center)
                        .background(Color.Black)
                ) {
                    val density = LocalDensity.current
                    val fontScale = LocalDensity.current.fontScale

                    val scale = 0.8f
                    CompositionLocalProvider(
                        LocalDensity provides Density(
                            density = density.density * scale,
                            fontScale = fontScale
                        ),
                    ) {
                        if (page != null && page.elements.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size((1.0 / scale * 360.0).dp, (1.0 / scale * 640).dp)
                                    .background(hexToColor(page.backgroundColor, colorNameToHex("background")))

                            ) {
                                var modifier = Modifier as Modifier
                                if (page.scrollable == "true") {
                                    modifier = modifier.verticalScroll(scrollState)
                                }
                                Column(
                                    modifier = modifier
                                        .padding(
                                            start = page.padding.left.dp,
                                            top = page.padding.top.dp,
                                            bottom = page.padding.bottom.dp,
                                            end = page.padding.right.dp
                                        )
                                        .fillMaxSize()
                                        .background(
                                            color = hexToColor(
                                                page.backgroundColor,
                                                colorNameToHex("background")
                                            )
                                        )
                                ) {
                                    RenderPage(page)
                                }
                            }
                        } else if (currentProject != null && currentProject.extension == "md") {
                            // markdown here
                            val md = MarkdownElement(
                                text = currentProject.currentFileContent.text,
                                part = "",
                                color = "#000000",
                                14.sp,
                                FontWeight.Normal,
                                TextAlign.Left, 0, 0, 0
                            )
                            Box(
                                modifier = Modifier
                                    .size((1.0 / scale * 360.0).dp, (1.0 / scale * 640).dp)
                                    .background(hexToColor("#F6F6F6"))

                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                        .background(color = hexToColor("#F6F6F6"))
                                ) {
                                    // we have to split the text to find out, where the images shall be rendered
                                    val imagePattern = Regex("!\\[([^\\]]*)\\]\\s*\\(\\s*([^\\s)]+)\\s*\"?([^\"\\)]*)\"?\\)")
                                    var currentIndex = 0
                                    val matches = imagePattern.findAll(md.text).toList()
                                    matches.forEach { match ->
                                        val startIndex = match.range.first
                                        val endIndex = match.range.last
                                        if (currentIndex < startIndex) {
                                            val textBeforeImage = md.text.substring(currentIndex, startIndex)
                                            Text(
                                                text = parseMarkdown(textBeforeImage),
                                                style = TextStyle(
                                                    color = hexToColor(
                                                        md.color,
                                                        colorNameToHex("onBackground")
                                                    )
                                                ),
                                                fontSize = md.fontSize,
                                                fontWeight = md.fontWeight,
                                                textAlign = md.textAlign
                                            )
                                        }
                                        val altText = match.groupValues[1]
                                        val imageUrl = match.groupValues[2].trim()
                                        at.crowdware.nocodelib.view.desktop.dynamicImageFromAssets(
                                            modifier = Modifier,
                                            imageUrl,
                                            "fit",
                                            "",
                                            0,
                                            0
                                        )

                                        currentIndex = endIndex + 1
                                    }
                                    val remainingText = md.text.substring(currentIndex)
                                    Text(
                                        text = parseMarkdown(remainingText),
                                        style = TextStyle(color = hexToColor(md.color, colorNameToHex("onBackground"))),
                                        fontSize = md.fontSize,
                                        fontWeight = md.fontWeight,
                                        textAlign = md.textAlign
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun renderText(element: TextElement) {
    CustomText(
        text = element.text,
        color = hexToColor( element.color, colorNameToHex("onBackground")),
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun ColumnScope.renderMarkdown(modifier: Modifier, element: MarkdownElement) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    if (element.part.isNotEmpty() && currentProject != null) {
        try {
            txt = File(currentProject.folder + "/parts", element.part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = element.text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(modifier = modifier,
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(element.color, colorNameToHex("onBackground"))),
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun RowScope.renderMarkdown(modifier: Modifier, element: MarkdownElement) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    if (element.part.isNotEmpty() && currentProject != null) {
        try {
            txt = File(currentProject.folder + "/parts", element.part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = element.text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(modifier = modifier,
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(element.color, colorNameToHex("onBackground"))),
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun renderMarkdown(element: MarkdownElement) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    if (element.part.isNotEmpty() && currentProject != null) {
        try {
            txt = File(currentProject.folder + "/parts", element.part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = element.text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(element.color, colorNameToHex("onBackground"))),
        fontSize = element.fontSize,
        fontWeight = element.fontWeight,
        textAlign = element.textAlign
    )
}

@Composable
fun renderButton(modifier: Modifier, element: ButtonElement) {
    var colors = ButtonDefaults.buttonColors()
    if(element.color.isNotEmpty() && element.backgroundColor.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor(element.backgroundColor), contentColor = hexToColor(element.color))
    else if(element.color.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor("primary"), contentColor = hexToColor(element.color))
    else if(element.backgroundColor.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor(element.backgroundColor), contentColor = hexToColor("onPrimary"))
    else
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor("primary"), contentColor = hexToColor("onPrimary"))
    Button(
        modifier = modifier.fillMaxWidth()
            .then(if(element.width > 0) Modifier.width(element.width.dp)else Modifier)
            .then(if(element.height > 0) Modifier.height(element.height.dp)else Modifier),
        colors = colors,
        onClick =  { handleButtonClick(element.link) }
    ) {
        Text(text = element.label)
    }
}

@Composable
fun renderColumn(element: ColumnElement) {
    Column(modifier = Modifier.padding(
        top = element.padding.top.dp,
        bottom = element.padding.bottom.dp,
        start = element.padding.left.dp,
        end = element.padding.right.dp
    )/*.fillMaxWidth()*/) {
        for (childElement in element.uiElements) {
            RenderUIElement(childElement)
        }
    }
}

@Composable
fun renderRow(element: RowElement) {
    Row(modifier = Modifier.padding(
        top = element.padding.top.dp,
        bottom = element.padding.bottom.dp,
        start = element.padding.left.dp,
        end = element.padding.right.dp)
        .fillMaxWidth()
        .then(if(element.height > 0) Modifier.height(element.height.dp) else Modifier)
        .then(if(element.width > 0) Modifier.width(element.width.dp) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween){
        for (childElement in element.uiElements) {
            RenderUIElement(childElement)
        }
    }
}

@Composable
fun RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
            renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(element)
        }
        is ButtonElement -> {
            renderButton(modifier = Modifier, element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicImageFromAssets(modifier = Modifier, element)
        }
        is SoundElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicSoundfromAssets(element.src)
        }
        is VideoElement -> {
            if (element.src.startsWith("http")) {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromUrl(modifier = Modifier)
            } else {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromAssets(modifier = Modifier, element.src)
            }
        }
        is YoutubeElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicYoutube()
        }
        is SceneElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicScene(modifier = Modifier, element.width, element.height)
        }
        else -> {
            println("Unknown element: $element")
        }
    }
}

@Composable
fun RowScope.RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
            renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(modifier = if(element.weight > 0)Modifier.weight(element.weight.toFloat())else Modifier, element)
        }
        is ButtonElement -> {
            renderButton(modifier = if(element.weight > 0)Modifier.weight(element.weight.toFloat())else Modifier, element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicImageFromAssets(
                modifier = if (element.weight > 0) Modifier.weight(
                    element.weight.toFloat()
                ) else Modifier, element
            )
        }
        is SoundElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicSoundfromAssets(element.src)
        }
        is SpacerElement -> {
            var mod = Modifier as Modifier

            if (element.amount > 0 )
                mod = mod.then(Modifier.width(element.amount.dp))
            if (element.weight > 0.0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))

            Spacer(modifier = mod)
        }
        is VideoElement -> {
            if (element.src.startsWith("http")) {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromUrl(
                    modifier = if (element.weight > 0) {
                        Modifier.weight(element.weight.toFloat())
                    } else {
                        Modifier
                    }
                )
            } else {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromAssets(
                    modifier = if (element.weight > 0) {
                        Modifier.weight(element.weight.toFloat())
                    } else {
                        Modifier
                    }, element.src
                )
            }
        }
        is YoutubeElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicYoutube(
                modifier = if (element.weight > 0) {
                    Modifier.weight(element.weight.toFloat())
                } else {
                    Modifier
                }
            )
        }
        is SceneElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicScene(
                modifier = if (element.weight > 0) {
                    Modifier.weight(element.weight.toFloat())
                } else {
                    Modifier
                }, element.width, element.height
            )
        }
        else -> {
            println("Unsupported element: $element")
        }
    }
}

@Composable
fun ColumnScope.RenderUIElement(element: UIElement) {
    when (element) {
        is TextElement -> {
           renderText(element)
        }
        is MarkdownElement -> {
            renderMarkdown(element)
        }
        is ButtonElement -> {
            renderButton(modifier = if(element.weight > 0)Modifier.weight(element.weight.toFloat())else Modifier, element)
        }
        is ColumnElement -> {
            renderColumn(element)
        }
        is RowElement -> {
            renderRow(element)
        }
        is ImageElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicImageFromAssets(
                modifier = if (element.weight > 0) {
                    Modifier.weight(element.weight.toFloat())
                } else {
                    Modifier
                }, element
            )
        }
        is SoundElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicSoundfromAssets(element.src)
        }
        is SpacerElement -> {
            var mod = Modifier as Modifier

            if (element.amount >0 )
                mod = mod.then(Modifier.height(element.amount.dp))
            if (element.weight > 0.0)
                mod = mod.then(Modifier.weight(element.weight.toFloat()))

            Spacer(modifier = mod)
        }
        is VideoElement -> {
            if (element.src.startsWith("http")) {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromUrl(
                    modifier = if (element.weight > 0) {
                        Modifier.weight(element.weight.toFloat())
                    } else {
                        Modifier
                    }
                )
            } else {
                at.crowdware.nocodelib.view.desktop.dynamicVideofromAssets(
                    modifier = if (element.weight > 0) {
                        Modifier.weight(element.weight.toFloat())
                    } else {
                        Modifier
                    }, element.src
                )
            }
        }
        is YoutubeElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicYoutube(
                modifier = if (element.weight > 0) {
                    Modifier.weight(element.weight.toFloat())
                } else {
                    Modifier
                }
            )
        }
        is SceneElement -> {
            at.crowdware.nocodelib.view.desktop.dynamicScene(
                modifier = if (element.weight > 0) {
                    Modifier.weight(element.weight.toFloat())
                } else {
                    Modifier
                }, element.width, element.height
            )
        }
        else -> {
            println("Unsupported element: $element")
        }
    }
}

fun colorNameToHex(colorName: String): String {
    val currentProject = GlobalProjectState.projectState
    if (currentProject != null) {
        return when (colorName) {
            "primary" -> {
                currentProject.app?.theme?.primary ?: ""
            }

            "onPrimary" -> {
                currentProject.app?.theme?.onPrimary ?: ""
            }

            "primaryContainer" -> {
                currentProject.app?.theme?.primaryContainer ?: ""
            }

            "onPrimaryContainer" -> {
                currentProject.app?.theme?.onPrimaryContainer ?: ""
            }

            "surface" -> {
                currentProject.app?.theme?.surface ?: ""
            }

            "onSurface" -> {
                currentProject.app?.theme?.onSurface ?: ""
            }

            "secondary" -> {
                currentProject.app?.theme?.secondary ?: ""
            }

            "onSecondary" -> {
                currentProject.app?.theme?.onSecondary ?: ""
            }

            "secondaryContainer" -> {
                currentProject.app?.theme?.secondaryContainer ?: ""
            }

            "onSecondaryContainer" -> {
                currentProject.app?.theme?.onSecondaryContainer ?: ""
            }

            "tertiary" -> {
                currentProject.app?.theme?.tertiary ?: ""
            }

            "onTertiary" -> {
                currentProject.app?.theme?.onTertiary ?: ""
            }

            "tertiaryContainer" -> {
                currentProject.app?.theme?.tertiaryContainer ?: ""
            }

            "onTertiaryContainer" -> {
                currentProject.app?.theme?.onTertiaryContainer ?: ""
            }

            "outline" -> {
                currentProject.app?.theme?.outline ?: ""
            }

            "outlineVariant" -> {
                currentProject.app?.theme?.outlineVariant ?: ""
            }

            "onErrorContainer" -> {
                currentProject.app?.theme?.onErrorContainer ?: ""
            }

            "onError" -> {
                currentProject.app?.theme?.onError ?: ""
            }

            "inverseSurface" -> {
                currentProject.app?.theme?.inverseSurface ?: ""
            }

            "inversePrimary" -> {
                currentProject.app?.theme?.inversePrimary ?: ""
            }

            "inverseOnSurface" -> {
                currentProject.app?.theme?.inverseOnSurface ?: ""
            }

            "background" -> {
                currentProject.app?.theme?.background ?: ""
            }

            "error" -> {
                currentProject.app?.theme?.error ?: ""
            }

            "scrim" -> {
                currentProject.app?.theme?.scrim ?: ""
            }

            else -> { "#000000" }
        }
    }
    return "#000000"
}

fun hexToColor(hex: String, default: String = "#000000"): Color {
    val currentProject = GlobalProjectState.projectState
    var value = hex
    if (hex.isEmpty()) {
        value = default
    }
    if(!hex.startsWith("#") && currentProject!= null) {
        when(hex) {
            "primary" -> {value = currentProject.app?.theme?.primary ?: "" }
            "onPrimary" -> {value = currentProject.app?.theme?.onPrimary ?: "" }
            "primaryContainer" -> {value = currentProject.app?.theme?.primaryContainer ?: "" }
            "onPrimaryContainer" -> {value = currentProject.app?.theme?.onPrimaryContainer ?: "" }
            "surface" -> {value = currentProject.app?.theme?.surface ?: "" }
            "onSurface" -> {value = currentProject.app?.theme?.onSurface ?: "" }
            "secondary" -> {value = currentProject.app?.theme?.secondary ?: "" }
            "onSecondary" -> {value = currentProject.app?.theme?.onSecondary ?: "" }
            "secondaryContainer" -> {value = currentProject.app?.theme?.secondaryContainer ?: "" }
            "onSecondaryContainer" -> {value = currentProject.app?.theme?.onSecondaryContainer ?: "" }
            "tertiary" -> {value = currentProject.app?.theme?.tertiary ?: "" }
            "onTertiary" -> {value = currentProject.app?.theme?.onTertiary ?: "" }
            "tertiaryContainer" -> {value = currentProject.app?.theme?.tertiaryContainer ?: "" }
            "onTertiaryContainer" -> {value = currentProject.app?.theme?.onTertiaryContainer ?: "" }
            "outline" -> {value = currentProject.app?.theme?.outline ?: "" }
            "outlineVariant" -> {value = currentProject.app?.theme?.outlineVariant ?: "" }
            "onErrorContainer" -> {value = currentProject.app?.theme?.onErrorContainer ?: "" }
            "onError" -> {value = currentProject.app?.theme?.onError ?: "" }
            "inverseSurface" -> {value = currentProject.app?.theme?.inverseSurface ?: "" }
            "inversePrimary" -> {value = currentProject.app?.theme?.inversePrimary ?: "" }
            "inverseOnSurface" -> {value = currentProject.app?.theme?.inverseOnSurface ?: "" }
            "background" -> {value = currentProject.app?.theme?.background ?: "" }
            "error" -> {value = currentProject.app?.theme?.error ?: "" }
            "scrim" -> {value = currentProject.app?.theme?.scrim ?: "" }
            else -> {value = default}
        }
    }

    val color = value.trimStart('#')
    return when (color.length) {
        6 -> {
            // Hex without alpha (e.g., "RRGGBB")
            val r = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b)
        }
        8 -> {
            // Hex with alpha (e.g., "AARRGGBB")
            val a = color.substring(0, 2).toIntOrNull(16) ?: return Color.Black
            val r = color.substring(2, 4).toIntOrNull(16) ?: return Color.Black
            val g = color.substring(4, 6).toIntOrNull(16) ?: return Color.Black
            val b = color.substring(6, 8).toIntOrNull(16) ?: return Color.Black
            Color(r, g, b, a)
        }
        else -> Color.Black
    }
}

@Composable
fun ColumnScope.RenderPage(page: Page) {
    for (element in page.elements) {
        RenderUIElement(element)
    }
}


@Composable
fun parseMarkdown(markdown: String): AnnotatedString {
    val builder = AnnotatedString.Builder()
    val lines = markdown.split("\n") // Process each line individually

    for (i in lines.indices) {
        val line = lines[i]
        var j = 0
        var inCodeBlock = false

        while (j < line.length) {
            if (line[j] == '`') {
                inCodeBlock = !inCodeBlock
                j++
                continue
            }

            if (inCodeBlock) {
                // Append text literally when in code mode
                val endOfCodeBlock = line.indexOf("`", j)
                if (endOfCodeBlock != -1) {
                    builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(line.substring(j, endOfCodeBlock))
                    }
                    j = endOfCodeBlock + 1
                    inCodeBlock = false // Close code mode
                } else {
                    // If no closing backtick is found, append till end of line
                    builder.withStyle(SpanStyle(fontFamily = FontFamily.Monospace)) {
                        append(line.substring(j))
                    }
                    j = line.length
                }
                continue
            }
            when {
                line.startsWith("###### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("###### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("##### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("##### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("#### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("#### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("### ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("### ").trim())
                    }
                    j = line.length
                }
                line.startsWith("## ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("## ").trim())
                    }
                    j = line.length
                }
                line.startsWith("# ", j) -> {
                    builder.withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                        append(line.removePrefix("# ").trim())
                    }
                    j = line.length
                }
                line.startsWith("![", j) -> {
                    // ignore images here
                    val endParen = line.indexOf(")", j)
                    if(endParen == -1)  // not found
                        j++
                    else
                        j = endParen + 1
                }
                line.startsWith("[", j) -> {

                    val endBracket = line.indexOf("]", j)
                    val startParen = line.indexOf("(", endBracket)
                    val endParen = line.indexOf(")", startParen)

                    if (endBracket != -1 && startParen == endBracket + 1 && endParen != -1) {
                        val linkText = line.substring(j + 1, endBracket)
                        val linkUrl = line.substring(startParen + 1, endParen)

                        builder.pushStringAnnotation(tag = "URL", annotation = linkUrl)
                        builder.withStyle(
                            SpanStyle(
                                color = ExtendedTheme.colors.linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(linkText)
                        }
                        builder.pop()
                        j = endParen + 1
                    } else {
                        builder.append(line[j])
                        j++
                    }
                }
                line.startsWith("<", j) && line.indexOf(">", j) > j -> {
                    // ignore html tags
                    val endParen = line.indexOf(">", j)
                    j = endParen + 1
                }
                line.startsWith("***", j) -> {
                    val endIndex = line.indexOf("***", j + 3)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 3, endIndex).trim())
                        }
                        j = endIndex + 3
                    } else {
                        builder.append("***")
                        j += 3
                    }
                }
                line.startsWith("**", j) -> {
                    val endIndex = line.indexOf("**", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("**")
                        j += 2
                    }
                }
                line.startsWith("*", j) -> {
                    val endIndex = line.indexOf("*", j + 1)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(line.substring(j + 1, endIndex).trim())
                        }
                        j = endIndex + 1
                    } else {
                        builder.append("*")
                        j += 1
                    }
                }
                line.startsWith("~~", j) -> {
                    val endIndex = line.indexOf("~~", j + 2)
                    if (endIndex != -1) {
                        builder.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(line.substring(j + 2, endIndex).trim())
                        }
                        j = endIndex + 2
                    } else {
                        builder.append("~~")
                        j += 2
                    }
                }
                line.startsWith("(c)", j, ignoreCase = true) -> {
                    builder.append("©")
                    j += 3
                }
                line.startsWith("(r)", j, ignoreCase = true) -> {
                    builder.append("®")
                    j += 3
                }
                line.startsWith("(tm)", j, ignoreCase = true) -> {
                    builder.append("™")
                    j += 4
                }
                else -> {
                    builder.append(line[j])
                    j++
                }
            }
        }

        if (i < lines.size - 1) {
            builder.append("\n")
        }
    }

    return builder.toAnnotatedString()
}

@Composable
expect fun dynamicImageFromAssets(modifier: Modifier = Modifier, src: String, scale: String, link: String, width: Int, height: Int)
@Composable
expect fun dynamicImageFromAssets(modifier: Modifier = Modifier, element: UIElement.ImageElement)
@Composable
expect fun dynamicSoundfromAssets(filename: String)
@Composable
expect fun dynamicVideofromAssets(modifier: Modifier = Modifier, filename: String)
@Composable
expect fun dynamicVideofromUrl(modifier: Modifier = Modifier)
@Composable
expect fun dynamicYoutube(modifier: Modifier = Modifier)
@Composable
expect fun dynamicScene(modifier: Modifier = Modifier, width: Int, height: Int)

fun handleButtonClick(link: String) {
    when {
        link.startsWith("page:") -> {
            val pageId = link.removePrefix("page:")
            at.crowdware.nocodelib.view.desktop.loadPage("$pageId.sml")
        }
        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            at.crowdware.nocodelib.view.desktop.openWebPage(url)
        }
        else -> {
            println("Unknown link type: $link")
        }
    }
}

@Composable
fun CustomText(
    text: String,
    color: Color,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start
) {
    // Determine the alignment for the Text
    val alignment = when (textAlign) {
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End -> Alignment.TopEnd
        else -> Alignment.TopStart
    }

    // Use a Box to apply the desired alignment
    Box(
        //modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment as Alignment
    ) {
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

expect fun loadPage(pageId: String)
expect fun openWebPage(url: String)
