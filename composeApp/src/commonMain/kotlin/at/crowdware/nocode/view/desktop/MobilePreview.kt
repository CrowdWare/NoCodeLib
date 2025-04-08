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

package at.crowdware.nocode.view.desktop


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
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
import at.crowdware.nocode.theme.ExtendedTheme
import at.crowdware.nocode.ui.HoverableIcon
import at.crowdware.nocode.ui.TooltipPosition
import at.crowdware.nocode.utils.*
//import at.crowdware.nocode.utils.UIElement.*
import at.crowdware.nocode.viewmodel.GlobalProjectState
import at.crowdware.nocode.viewmodel.ProjectState
import java.io.File

@Composable
fun mobilePreview(currentProject: ProjectState?) {
    var node: SmlNode? = if (currentProject?.isPageLoaded == true) currentProject.parsedPage else null
    val scrollState = rememberScrollState()
    val lang = currentProject?.lang

    if (node == null && currentProject != null) {
        // in case of syntax error we keep showing the last page
        node = currentProject.cachedPage
    }

    Column(modifier = Modifier.width(430.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
        Row() {
            BasicText(
                text = "Mobile Preview",
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                style = TextStyle(color = MaterialTheme.colors.onPrimary),
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.height(24.dp)) {
                HoverableIcon(
                    painter = painterResource("drawable/landscape.xml"),
                    onClick = { currentProject?.isPortrait = false },
                    tooltipText = "Desktop Preview",
                    isSelected = false,
                    tooltipPosition = TooltipPosition.Left
                )
            }
        }
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
                        if (node != null && node.children.isNotEmpty() && currentProject?.extension == "sml") {
                            val pageBackgroundColor = hexToColor(getStringValue(node, "background", "background"))

                            Box(
                                modifier = Modifier
                                    .size((1.0 / scale * 360.0).dp, (1.0 / scale * 640).dp)
                                    .background(pageBackgroundColor)

                            ) {
                                var modifier = Modifier as Modifier
                                val scrollableProperty = node.properties["scrollable"] as? PropertyValue.StringValue
                                if (scrollableProperty?.value == "true") {
                                    modifier = modifier.verticalScroll(scrollState)
                                }
                                val padding = getPadding(node)
                                Column(
                                    modifier = modifier
                                        .padding(
                                            start = padding.left.dp,
                                            top = padding.top.dp,
                                            bottom = padding.bottom.dp,
                                            end = padding.right.dp
                                        )
                                        .fillMaxSize()
                                        .background(color = pageBackgroundColor)
                                ) {
                                    RenderPage(node, lang!!)
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
                                        dynamicImageFromAssets(
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
fun renderText(node: SmlNode) {
    CustomText(
        text = getStringValue(node,"text", ""),
        color = hexToColor( getStringValue(node, "color", "onBackground")),
        fontSize = getIntValue(node, "fontSize", 14).sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node)
    )
}

@Composable
fun ColumnScope.renderMarkdown(modifier: Modifier, node: SmlNode, lang: String) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    val part = getStringValue(node, "part", "")
    val text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (part.isNotEmpty() && currentProject != null) {
        var dir = "parts"
        if(lang.isNotEmpty()) {
            dir += "-$lang"
        }

        try {
            txt = File(currentProject.folder + "/$dir", part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(modifier = modifier.fillMaxWidth(),
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(color)),
        fontSize = fontSize.sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node)
    )
}

@Composable
fun RowScope.renderMarkdown(modifier: Modifier, node: SmlNode, lang: String) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    val part = getStringValue(node, "part", "")
    val text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (part.isNotEmpty() && currentProject != null) {
        var dir = "parts"
        if (lang.isNotEmpty()) {
            dir += "-$lang"
        }
        try {
            txt = File(currentProject.folder + "/$dir", part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(modifier = modifier,
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(color, colorNameToHex("onBackground"))),
        fontSize = fontSize.sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node)
    )
}

@Composable
fun renderMarkdown(node: SmlNode, lang: String) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    val part = getStringValue(node, "part", "")
    val text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (part.isNotEmpty() && currentProject != null) {
        var dir = "parts"
        if (lang.isNotEmpty()) {
            dir += "-$lang"
        }
        try {
            txt = File(currentProject.folder + "/$dir", part).readText()
        } catch(e: Exception) {
            println("An error occurred in RenderMarkdown: ${e.message}")
        }
    } else {
        txt = text
    }
    val parsedMarkdown = parseMarkdown(txt)
    Text(
        text = parsedMarkdown,
        style = TextStyle(color = hexToColor(color, colorNameToHex("onBackground"))),
        fontSize = fontSize.sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node)
    )
}

@Composable
fun renderButton(modifier: Modifier, node: SmlNode) {
    var colors: ButtonColors
    val width = getIntValue(node, "width", 0)
    val height = getIntValue(node, "height", 0)
    val color = getStringValue(node, "color", "")
    val backgroundColor = getStringValue(node, "backgroundColor", "")

    if(color.isNotEmpty() && backgroundColor.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor(backgroundColor), contentColor = hexToColor(color))
    else if(color.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor("primary"), contentColor = hexToColor(color))
    else if(backgroundColor.isNotEmpty())
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor(backgroundColor), contentColor = hexToColor("onPrimary"))
    else
        colors = ButtonDefaults.buttonColors(backgroundColor = hexToColor("primary"), contentColor = hexToColor("onPrimary"))
    Button(
        modifier = modifier
            .then(if (width > 0) Modifier.width(width.dp) else Modifier.fillMaxWidth())
            .then(if (height > 0) Modifier.height(height.dp) else Modifier) ,
        colors = colors,
        onClick =  { handleButtonClick(getStringValue(node,"link", "")) }
    ) {
        Text(text = getStringValue(node, "label", ""))
    }
}

@Composable
fun renderColumn(modifier: Modifier, node: SmlNode, lang: String) {
    val padding = getPadding(node)
    val background = getStringValue(node, "background", "background")

    Column(modifier = modifier.background(hexToColor(background))
        .padding(
        top = padding.top.dp,
        bottom = padding.bottom.dp,
        start = padding.left.dp,
        end = padding.right.dp
    )) {
        for (childElement in node.children) {
            RenderUIElement(childElement, lang)
        }
    }
}

@Composable
fun renderRow(node: SmlNode, lang: String) {
    val padding = getPadding(node)
    val height = getIntValue(node, "height", 0)
    val width = getIntValue(node, "width", 0)
    val background = getStringValue(node, "background", "background")

    Row(
        modifier = Modifier.background(hexToColor(background))
            .padding(
            top = padding.top.dp,
            bottom = padding.bottom.dp,
            start = padding.left.dp,
            end = padding.right.dp
        )
            .fillMaxWidth()
            .then(if (height > 0) Modifier.height(height.dp) else Modifier)
            .then(if (width > 0) Modifier.width(width.dp) else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (childElement in node.children) {
            RenderUIElement(childElement, lang)
        }
    }
}

@Composable
fun renderBox(node: SmlNode, lang: String) {
    val padding = getPadding(node)
    val height = getIntValue(node, "height", 0)
    val width = getIntValue(node, "width", 0)
    val background = getStringValue(node, "background", "background")

    Box(modifier = Modifier.background(hexToColor(background))
        .padding(
        top = padding.top.dp,
        bottom = padding.bottom.dp,
        start = padding.left.dp,
        end = padding.right.dp)
        .then(if(height > 0) Modifier.height(height.dp) else Modifier.fillMaxHeight())
        .then(if(width > 0) Modifier.width(width.dp) else Modifier.fillMaxWidth())){
        for (child in node.children) {
            RenderUIElement(child, lang)
        }
    }
}

@Composable
fun renderLazyColumn(modifier: Modifier, node: SmlNode, lang: String) {
    Column(modifier = modifier) {
        for (child in node.children) {
            RenderUIElement(child, lang)
        }
    }
}

@Composable
fun renderLazyRow(node: SmlNode, lang: String) {
    val height = getIntValue(node, "height", 0)
    Row (modifier = if(height > 0) Modifier.height(height.dp) else Modifier) {
        for (child in node.children) {
            RenderUIElement(child, lang)
        }
    }
}

@Composable
fun RowScope.RenderUIElement(node: SmlNode, lang: String) {
    val weight = getIntValue(node, "weight", 0)

    when (node.name) {
        "Column" -> {
            renderColumn(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang)
        }
        "Row" -> {
            renderRow(node, lang)
        }
        "Box" -> {
            renderBox(node, lang)
        }
        "Spacer" -> {
            var mod = Modifier as Modifier
            val amount = getIntValue(node, "amount", 0)
            if (amount > 0 )
                mod = mod.then(Modifier.width(amount.dp))
            if (weight > 0.0)
                mod = mod.then(Modifier.weight(weight.toFloat()))

            Spacer(modifier = mod)
        }
        "Text" -> {
            renderText(node)
        }
        "Markdown" -> {
            renderMarkdown(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node, lang = lang)
        }
        "Button" -> {
            renderButton(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier.weight(1f), node)
        }
        "Image" -> {
            dynamicImageFromAssets(modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node)
        }
        "LazyColumn" -> {
            renderLazyColumn(modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang)
        }
        "LazyRow" -> {
            renderLazyRow(node, lang)
        }
        "AsyncImage" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            val scale = getStringValue(node, "scale", "")
            asyncImage(
                modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier,"", scale, "", width, height
            )
        }
        "Sound" -> {
            val src = getStringValue(node, "src", "")
            dynamicSoundfromAssets(src)
        }
        "Video" -> {
            val src = getStringValue(node, "src", "")
            if (src.startsWith("http")) {
                dynamicVideofromUrl(
                    modifier = if (weight > 0) {
                        Modifier.weight(weight.toFloat())
                    } else {
                        Modifier
                    }
                )
            } else {
                dynamicVideofromAssets(
                    modifier = if (weight > 0) {
                        Modifier.weight(weight.toFloat())
                    } else {
                        Modifier
                    }, src
                )
            }
        }
        "Youtube" -> {
            dynamicYoutube(
                modifier = if (weight > 0) {
                    Modifier.weight(weight.toFloat())
                } else {
                    Modifier
                }
            )
        }
        "Scene" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            dynamicScene(
                modifier = if (weight > 0) {
                    Modifier.weight(weight.toFloat())
                } else {
                    Modifier
                }, width, height
            )
        }
        else -> {
            println("Unsupported element: ${node.name}")
        }
    }
}

fun String.toAlignment(): Alignment {
    return when (this) {
        "topStart" -> Alignment.TopStart
        "topCenter" -> Alignment.TopCenter
        "topEnd" -> Alignment.TopEnd
        "centerStart" -> Alignment.CenterStart
        "center" -> Alignment.Center
        "centerEnd" -> Alignment.CenterEnd
        "bottomStart" -> Alignment.BottomStart
        "bottomCenter" -> Alignment.BottomCenter
        "bottomEnd" -> Alignment.BottomEnd
        else -> Alignment.TopStart // Default fallback
    }
}

@Composable
fun BoxScope.RenderUIElement(node: SmlNode, lang: String) {
    when (node.name) {
        "Text" -> {
           renderText(node)
        }
        "Markdown" -> {
            renderMarkdown(node, lang)
        }
        "Button" -> {
            renderButton(modifier = Modifier, node)
        }
        "Column" -> {
            renderColumn(modifier = Modifier, node, lang)
        }
        "Row" -> {
            renderRow(node, lang)
        }
        "Box" -> {
            renderBox(node, lang)
        }
        "Spacer" -> {
            var mod = Modifier as Modifier
            val amount = getIntValue(node, "amount", 0)
            if (amount >0 )
                mod = mod.then(Modifier.height(amount.dp))
            Spacer(modifier = mod)
        }
        "Image" -> {
            val align = getStringValue(node, "align", "")
            val alignment = if (align.isNotEmpty()) align.toAlignment() else Alignment.TopStart
            dynamicImageFromAssets(modifier = Modifier.align(alignment), node = node)
        }
        "LazyColumn" -> {
            renderLazyColumn(modifier = Modifier, node = node, lang = lang)
        }
        "LazyRow" -> {
            renderLazyRow(node, lang)
        }
        "AsyncImage" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            val scale = getStringValue(node, "scale", "")
            asyncImage(modifier = Modifier, "", scale, "", width, height)
        }
        "SoundElement" -> {
            val src = getStringValue(node, "src", "")
            dynamicSoundfromAssets(src)
        }

        "VideoElement" -> {
            val src = getStringValue(node, "src", "")
            if (src.startsWith("http")) {
                dynamicVideofromUrl(modifier = Modifier)
            } else {
                dynamicVideofromAssets(modifier = Modifier, src)
            }
        }
        "Youtube" -> {
            dynamicYoutube(modifier = Modifier)
        }
        "Scene" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            dynamicScene(modifier = Modifier, width, height)
        }
        else -> {
            //println("Unsupported node: $node")
        }
    }
}

@Composable
fun ColumnScope.RenderUIElement(node: SmlNode, lang: String) {
    val weight = getIntValue(node, "weight", 0)
    when (node.name) {
        "Column" -> {
            renderColumn(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang)
        }
        "Row" -> {
            renderRow(node, lang)
        }
        "Spacer" -> {
            var mod = Modifier as Modifier
            val amount = getIntValue(node, "amount", 0)
            if (amount > 0 )
                mod = mod.then(Modifier.height(amount.dp))
            if (weight > 0.0)
                mod = mod.then(Modifier.weight(weight.toFloat()))

            Spacer(modifier = mod)
        }
        "Markdown" -> {
            renderMarkdown(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node, lang)
        }
        "Text" -> {
            renderText(node)
        }
        "Button" -> {
            renderButton(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node)
        }
        "Box" -> {
            renderBox(node, lang)
        }
        "Image" -> {
            dynamicImageFromAssets(modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node)
        }
        "LazyColumn" -> {
            renderLazyColumn(modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang)
        }
        "LazyRow" -> {
            renderLazyRow(node, lang)
        }
        "AsyncImage" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            val scale = getStringValue(node, "scale", "")
            asyncImage(
                modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier,"", scale, "", width, height
            )
        }
        "Sound" -> {
            val src = getStringValue(node, "src", "")
            dynamicSoundfromAssets(src)
        }

        "Video" -> {
            val src = getStringValue(node, "src", "")
            if (src.startsWith("http")) {
                dynamicVideofromUrl(
                    modifier = if (weight > 0) {
                        Modifier.weight(weight.toFloat())
                    } else {
                        Modifier
                    }
                )
            } else {
                dynamicVideofromAssets(
                    modifier = if (weight > 0) {
                        Modifier.weight(weight.toFloat())
                    } else {
                        Modifier
                    }, src
                )
            }
        }
        "Youtube" -> {
            dynamicYoutube(
                modifier = if (weight > 0) {
                    Modifier.weight(weight.toFloat())
                } else {
                    Modifier
                }
            )
        }
        "Scene" -> {
            val width = getIntValue(node, "width", 0)
            val height = getIntValue(node, "height", 0)
            dynamicScene(
                modifier = if (weight > 0) {
                    Modifier.weight(weight.toFloat())
                } else {
                    Modifier
                }, width, height
            )
        }
        else -> {
            //println("Unsupported node: ${node.name}")
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
            "onBackground" -> {value = currentProject.app?.theme?.onBackground ?: "" }
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
fun ColumnScope.RenderPage(node: SmlNode, lang: String) {
    for (child in node.children) {
        RenderUIElement(child, lang)
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
expect fun dynamicImageFromAssets(modifier: Modifier = Modifier, node: SmlNode)
@Composable
expect fun asyncImage(modifier: Modifier = Modifier, src: String, scale: String, link: String, width: Int, height: Int)
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
            at.crowdware.nocode.view.desktop.loadPage("$pageId.sml")
        }
        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            at.crowdware.nocode.view.desktop.openWebPage(url)
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
        modifier = Modifier.fillMaxWidth(),
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
