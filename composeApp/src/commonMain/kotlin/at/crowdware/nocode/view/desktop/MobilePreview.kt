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


import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
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
import at.crowdware.nocode.viewmodel.GlobalProjectState
import at.crowdware.nocode.viewmodel.ProjectState
import java.io.File
import java.util.prefs.Preferences

@Composable
fun mobilePreview(
    currentProject: ProjectState?
) {
    var node: SmlNode? = if (currentProject?.isPageLoaded == true) currentProject.parsedPage else null
    val scrollState = rememberScrollState()
    val clickCount = remember { mutableStateOf(0) }
    var lang by remember { mutableStateOf("en") }

    if (node == null && currentProject != null) {
        // in case of syntax error we keep showing the last page
        node = currentProject.cachedPage
    }

    Column(modifier = Modifier.width(430.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)) {
        var expanded by remember { mutableStateOf(false) }
        val languages = currentProject?.getLanguages().orEmpty()

        Row() {
            BasicText(
                text = "Mobile Preview",
                modifier = Modifier.padding(8.dp),
                maxLines = 1,
                style = TextStyle(color = MaterialTheme.colors.onPrimary),
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .background(MaterialTheme.colors.surface.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lang,
                            style = TextStyle(color = MaterialTheme.colors.onPrimary),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Arrow",
                            tint = MaterialTheme.colors.onPrimary
                        )
                    }
                }

                // Dropdown-Content
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    languages.forEach { language ->
                        DropdownMenuItem(onClick = {
                            lang = language
                            expanded = false
                        }) {
                            Text(text = language)
                        }
                    }
                }
            }
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
                                val scrollableProperty = getBoolValue(node, "scrollable", false)
                                if (scrollableProperty) {
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
                                    RenderPage(node, lang!!, "", currentProject, clickCount)
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
                                    Text(
                                        text = parseMarkdown(md.text),
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
fun renderText(node: SmlNode, currentProject: ProjectState, lang: String) {
    CustomText(
        text = getStringValue(node,"text", ""),
        color = hexToColor( getStringValue(node, "color", "onBackground")),
        fontSize = getIntValue(node, "fontSize", 14).sp,
        fontWeight = getFontWeight(node),
        textAlign = getTextAlign(node),
        currentProject = currentProject,
        lang = lang
    )
}

@Composable
fun ColumnScope.renderMarkdown(modifier: Modifier, node: SmlNode, lang: String, dataItem: Any) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    //val part = getStringValue(node, "part", "")
    var text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (text.startsWith("part:")) {
        val part = text.substringAfter("part:")
        if (part.isNotEmpty() && currentProject != null) {
            try {
                val file = File(currentProject.folder, "parts/$part-$lang.md")
                txt = file.readText()
            } catch (e: Exception) {
                println("An error occurred in RenderMarkdown: ${e.message}")
            }
        } else if (text.startsWith("string:")) {
            if(currentProject != null) {
                txt = translate(text.substringAfter("string:"), currentProject, lang)
            }
        } else if (text.startsWith("<") && text.endsWith(">")) {
            val fieldName = text.substring(1, text.length - 1)
            if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
                val des = dataItem[fieldName] as? String
                text = "$des"
            }
            txt = text
        }
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
fun RowScope.renderMarkdown(modifier: Modifier, node: SmlNode, lang: String, dataItem: Any) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    var text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (text.startsWith("part:")) {
        val part = text.substringAfter("part:")
        if (part.isNotEmpty() && currentProject != null) {
            try {
                val file = File(currentProject.folder, "parts/$part-$lang.md")
                txt = file.readText()
            } catch (e: Exception) {
                println("An error occurred in RenderMarkdown: ${e.message}")
            }
        } else if (text.startsWith("string:")) {
            if(currentProject != null) {
                txt = translate(text.substringAfter("string:"), currentProject, lang)
            }
        } else if (text.startsWith("<") && text.endsWith(">")) {
            val fieldName = text.substring(1, text.length - 1)
            if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
                val des = dataItem[fieldName] as? String
                text = "$des"
            }
            txt = text
        }
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
fun renderMarkdown(node: SmlNode, lang: String, dataItem: Any) {
    var txt = ""
    val currentProject = GlobalProjectState.projectState
    var text = getStringValue(node, "text", "")
    val color = getStringValue(node, "color", "onBackground")
    val fontSize = getIntValue(node, "fontSize", 16)

    if (text.startsWith("part:")) {
        val part = text.substringAfter("part:")
        if (part.isNotEmpty() && currentProject != null) {
            try {
                val file = File(currentProject.folder, "parts/$part-$lang.md")
                txt = file.readText()
            } catch (e: Exception) {
                println("An error occurred in RenderMarkdown: ${e.message}")
            }
        } else if (text.startsWith("string:")) {
            if(currentProject != null) {
                txt = translate(text.substringAfter("string:"), currentProject, lang)
            }
        } else if (text.startsWith("<") && text.endsWith(">")) {
            val fieldName = text.substring(1, text.length - 1)
            if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
                val des = dataItem[fieldName] as? String
                text = "$des"
            }
            txt = text
        }
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
fun renderButton(modifier: Modifier, node: SmlNode, dataItem: Any, clickCount: MutableState<Int>, datasourceId: String, currentProject: ProjectState, lang: String) {
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
        onClick =  { handleButtonClick(getStringValue(node,"link", ""),
            dataItem = dataItem,
            clickCount = clickCount,
            datasourceId = datasourceId,
            currentProject = currentProject) }
    ) {
        val label = translate(getStringValue(node, "label", ""), currentProject, lang)
        Text(text = label)
    }
}

@Composable
fun renderColumn(
    modifier: Modifier,
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
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
            RenderUIElement(childElement, lang, dataItem = dataItem, datasourceId, currentProject, clickCount)
        }
    }
}

@Composable
fun renderRow(
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
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
            RenderUIElement(childElement, lang, dataItem = dataItem, datasourceId, currentProject, clickCount)
        }
    }
}

@Composable
fun renderBox(
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
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
            RenderUIElement(child, lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun renderLazyColumn(
    modifier: Modifier,
    node: SmlNode,
    lang: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    val EmptyDataItem = object {}
    val datasource = getStringValue(node, "datasource", "")
    val rawData = currentProject.data[datasource]
    if (rawData == null) {
        CircularProgressIndicator(modifier = modifier)
        return
    }

    val filteredData = applyFilter(rawData, getStringValue(node, "filter", ""), currentProject)
    val sortedData = applyOrder(filteredData, getStringValue(node, "order", ""))
    val finalData = applyLimit(sortedData, getIntValue(node, "limit", 0))

    val padding = getPadding(node)

    AnimatedContent(
        modifier = modifier.padding(padding.left.dp, padding.top.dp, padding.right.dp, padding.bottom.dp),
        targetState = clickCount.value to finalData,
        transitionSpec = {
            fadeIn(tween(300)) with fadeOut(tween(300))
        },
        label = "AnimatedLazyColumn"
    ) { (_, animatedList) ->
        if (animatedList.isEmpty()) {
            for (child in node.children) {
                if (child.name == "LazyNoContent") {
                    Box(modifier = Modifier) {
                        RenderUIElement(
                            node = child,
                            lang = lang,
                            dataItem = EmptyDataItem,
                            datasourceId = datasource,
                            currentProject = currentProject,
                            clickCount = clickCount
                        )
                    }
                }
            }
        } else {
            for (child in node.children) {
                if (child.name == "LazyContent") {
                    LazyColumn(modifier = modifier) {
                        items(animatedList, key = { it.hashCode() }) { dataItem ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                for (subChild in child.children) {
                                    RenderUIElement(
                                        node = subChild,
                                        lang = lang,
                                        dataItem = dataItem,
                                        datasourceId = datasource,
                                        currentProject = currentProject,
                                        clickCount = clickCount
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


fun applyFilter(
    data: List<Any>,
    filter: String?,
    currentProject: ProjectState
): List<Any> {
    if (filter == null) return data
    val regex = Regex("""(inList|notInList):(\w+)\[(\w+)]""")
    val match = regex.find(filter) ?: return data

    val (mode, listName, paramName) = match.destructured
    val prefs = DesktopPreferences(currentProject.prefsFile)
    val values = prefs.getStringSet(listName, emptySet())

    return when (mode) {
        "inList" -> data.filter {
            (it as? Map<*, *>)?.get(paramName).toString() in values
        }
        "notInList" -> data.filter {
            (it as? Map<*, *>)?.get(paramName).toString() !in values
        }
        else -> data
    }
}

fun applyOrder(data: List<Any>, order: String?): List<Any> {
    if (order == null) return data
    val (field, dir) = order.split(" ").let { it[0] to (it.getOrNull(1) ?: "asc") }

    val sorted = data.sortedBy {
        (it as? Map<*, *>)?.get(field) as? Comparable<Any>
    }
    return if (dir == "desc") sorted.reversed() else sorted
}

fun applyLimit(data: List<Any>, limit: Int?): List<Any> {
    return if (limit != null && limit > 0) data.take(limit) else data
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun renderLazyRow(
    modifier: Modifier,
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    val height = getIntValue(node, "height", 0)
    val EmptyDataItem = object {}
    val datasource = getStringValue(node, "datasource", "")
    val rawData = currentProject.data[datasource]
    if (rawData == null) {
        CircularProgressIndicator(modifier = modifier)
        return
    }

    val filteredData = applyFilter(rawData, getStringValue(node, "filter", ""), currentProject)
    val sortedData = applyOrder(filteredData, getStringValue(node, "order", ""))
    val finalData = applyLimit(sortedData, getIntValue(node, "limit", 0))

    val padding = getPadding(node)

    AnimatedContent(
        modifier = modifier.padding(padding.left.dp, padding.top.dp, padding.right.dp, padding.bottom.dp)
            .then(if(height > 0) Modifier.height(height.dp) else Modifier),
        targetState = clickCount.value to finalData,
        transitionSpec = {
            fadeIn(tween(300)) with fadeOut(tween(300))
        },
        label = "AnimatedLazyColumn"
    ) { (_, animatedList) ->
        if (animatedList.isEmpty()) {
            for (child in node.children) {
                if (child.name == "LazyNoContent") {
                    Box(modifier = Modifier) {
                        RenderUIElement(
                            node = child,
                            lang = lang,
                            dataItem = EmptyDataItem,
                            datasourceId = datasource,
                            currentProject = currentProject,
                            clickCount = clickCount
                        )
                    }
                }
            }
        } else {
            for (child in node.children) {
                if (child.name == "LazyContent") {
                    LazyRow(modifier = modifier) {
                        items(animatedList, key = { it.hashCode() }) { dataItem ->
                            Box(modifier = Modifier.animateItemPlacement()) {
                                for (subChild in child.children) {
                                    RenderUIElement(
                                        node = subChild,
                                        lang = lang,
                                        dataItem = dataItem,
                                        datasourceId = datasource,
                                        currentProject = currentProject,
                                        clickCount = clickCount
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
fun RowScope.RenderUIElement(
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    val weight = getIntValue(node, "weight", 0)

    when (node.name) {
        "Column" -> {
            renderColumn(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Row" -> {
            renderRow(node, lang, dataItem = dataItem, datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Box" -> {
            renderBox(node, lang, dataItem = dataItem, datasourceId, currentProject = currentProject, clickCount = clickCount)
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
            renderText(node, currentProject, lang)
        }
        "Markdown" -> {
            renderMarkdown(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node, lang = lang, dataItem = dataItem)
        }
        "Button" -> {
            renderButton(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier.weight(1f), node, dataItem, clickCount, datasourceId, currentProject, lang)
        }
        "Image" -> {
            dynamicImageFromAssets(
                modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier,
                node,
                dataItem,
                clickCount,
                datasourceId,
                currentProject
            )
        }
        "LazyColumn" -> {
            renderLazyColumn(
                modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier,
                node = node,
                lang = lang,
                currentProject,
                clickCount
            )
        }
        "LazyRow" -> {
            renderLazyRow(
                modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier,
                node = node,
                lang = lang,
                dataItem = dataItem,
                datasourceId = datasourceId,
                currentProject,
                clickCount
            )
        }
        "AsyncImage" -> {
            asyncImage(
                modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier,
                node,
                dataItem,
                clickCount,
                datasourceId,
                currentProject
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
fun BoxScope.RenderUIElement(
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    when (node.name) {
        "Text" -> {
           renderText(node, currentProject, lang)
        }
        "Markdown" -> {
            renderMarkdown(node, lang, dataItem)
        }
        "Button" -> {
            renderButton(modifier = Modifier, node, dataItem, clickCount, datasourceId, currentProject, lang)
        }
        "Column" -> {
            renderColumn(modifier = Modifier, node, lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Row" -> {
            renderRow(node, lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Box" -> {
            renderBox(node, lang, dataItem = dataItem, datasourceId, currentProject = currentProject, clickCount = clickCount)
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
            dynamicImageFromAssets(
                modifier = Modifier.align(alignment),
                node = node,
                dataItem = dataItem,
                clickCount = clickCount,
                datasourceId = datasourceId,
                currentProject = currentProject
            )
        }
        "LazyColumn" -> {
            renderLazyColumn(modifier = Modifier, node = node, lang = lang, currentProject, clickCount)
        }
        "LazyRow" -> {
            renderLazyRow(modifier = Modifier, node = node, lang = lang, dataItem = dataItem, datasourceId = datasourceId, currentProject, clickCount)
        }
        "AsyncImage" -> {
            asyncImage(modifier = Modifier, node, dataItem, clickCount, datasourceId, currentProject)
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
fun ColumnScope.RenderUIElement(
    node: SmlNode,
    lang: String,
    dataItem: Any,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    val weight = getIntValue(node, "weight", 0)
    when (node.name) {
        "Column" -> {
            renderColumn(modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Row" -> {
            renderRow(node, lang, dataItem = dataItem, datasourceId, currentProject, clickCount)
        }
        "LazyColumn" -> {
            val effectiveModifier = if (weight > 0) { Modifier.weight(weight.toFloat()) } else { Modifier }
            renderLazyColumn(
                modifier = effectiveModifier,
                node = node,
                lang = lang,
                currentProject = currentProject,
                clickCount = clickCount
            )
        }
        "LazyRow" -> {
            renderLazyRow(modifier = if(weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node = node, lang = lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
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
            renderMarkdown(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node, lang, dataItem)
        }
        "Text" -> {
            renderText(node, currentProject, lang)
        }
        "Button" -> {
            renderButton(modifier = if(weight > 0)Modifier.weight(weight.toFloat()) else Modifier, node, dataItem, clickCount, datasourceId, currentProject, lang)
        }
        "Box" -> {
            renderBox(node, lang, dataItem = dataItem, datasourceId = datasourceId, currentProject = currentProject, clickCount = clickCount)
        }
        "Image" -> {
            dynamicImageFromAssets(modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node, dataItem, clickCount, datasourceId, currentProject)
        }
        "AsyncImage" -> {
            asyncImage(modifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier, node, dataItem, clickCount, datasourceId, currentProject)
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
fun ColumnScope.RenderPage(
    node: SmlNode,
    lang: String,
    datasourceId: String,
    currentProject: ProjectState,
    clickCount: MutableState<Int>
) {
    var dataItem: Any = emptyMap<String, Any>()
    for (child in node.children) {
        RenderUIElement(child, lang, dataItem = dataItem, datasourceId, currentProject, clickCount)
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
expect fun dynamicImageFromAssets(
    modifier: Modifier = Modifier,
    node: SmlNode,
    dataItem: Any,
    clickCount: MutableState<Int>,
    datasourceId: String,
    currentProject: ProjectState
)
@Composable
expect fun asyncImage(
    modifier: Modifier = Modifier,
    node: SmlNode,
    dataItem: Any,
    clickCount: MutableState<Int>,
    datasourceId: String,
    currentProject: ProjectState
)
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

fun handleButtonClick(link: String,
                      dataItem: Any,
                      clickCount: MutableState<Int>,
                      datasourceId: String,
                      currentProject: ProjectState) {
    val prefs = DesktopPreferences(currentProject.prefsFile)

    when {
        link.startsWith("page:") -> {
            val pageId = link.removePrefix("page:")
            loadPage("$pageId.sml")
        }

        link.startsWith("web:") -> {
            val url = link.removePrefix("web:")
            openWebPage(url)
        }
        link.startsWith("add:") -> {
            val (listName, uuid) = extractListAndUUID(link.removePrefix("add:"), dataItem)

            val set = prefs.getStringSet(listName).toMutableSet()
            set.add(uuid)
            prefs.putStringSet(listName, set)

            clickCount.value++ // trigger reload

            val current = currentProject.data[datasourceId] ?: emptyList()
            val newList = current.toList()
            currentProject.data = currentProject.data.toMutableMap().apply {
                put(datasourceId, newList)
            }
        }
        link.startsWith("remove:") -> {

            val (listName, uuid) = extractListAndUUID(link.removePrefix("remove:"), dataItem)

            val set = prefs.getStringSet(listName).toMutableSet()
            set.remove(uuid)
            prefs.putStringSet(listName, set)

            clickCount.value++ // trigger reload

            val current = currentProject.data[datasourceId] ?: emptyList()
            val newList = current.toList()
            currentProject.data = currentProject.data.toMutableMap().apply {
                put(datasourceId, newList)
            }
        }
        else -> {
            println("Unknown link type: $link")
        }
    }
}

fun extractListAndUUID(linkPart: String, dataItem: Any): Pair<String, String> {
    val regex = Regex("""(.*?)\[(.*?)]""")
    val match = regex.find(linkPart)
    val listName = match?.groupValues?.get(1) ?: "default"
    var uuid = match?.groupValues?.get(2) ?: ""

    if (uuid.startsWith("<") && uuid.endsWith(">")) {
        val fieldName = uuid.substring(1, uuid.length - 1)
        if (dataItem is Map<*, *>) {
            uuid = dataItem[fieldName] as? String ?: ""
        }
    }

    return listName to uuid
}

@Composable
fun CustomText(
    text: String,
    color: Color,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    currentProject: ProjectState,
    lang: String
) {
    // Determine the alignment for the Text
    val alignment = when (textAlign) {
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End -> Alignment.TopEnd
        else -> Alignment.TopStart
    }

    val txt = translate(text, currentProject, lang)

    // Use a Box to apply the desired alignment
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment as Alignment
    ) {
        Text(
            text = txt,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}

private fun translate(
    text: String,
    currentProject: ProjectState,
    lang: String
): String {
    var txt = text
    if (text.startsWith("string:")) {
        val file = File(currentProject.folder, "translations/Strings-$lang.sml")
        if (file.exists()) {
            val content = file.readText()
            val (parsedStrings, _) = parseSML(content)
            if (parsedStrings != null) {
                txt = getStringValue(parsedStrings, text.substringAfter("string:"), "")
            }
        }
    }
    return txt
}

expect fun loadPage(pageId: String)
expect fun openWebPage(url: String)
