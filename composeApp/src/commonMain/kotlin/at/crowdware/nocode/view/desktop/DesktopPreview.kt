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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocode.ui.HoverableIcon
import at.crowdware.nocode.ui.TooltipPosition
import at.crowdware.nocode.utils.*
import at.crowdware.nocode.view.desktop.*
import at.crowdware.nocode.viewmodel.ProjectState

@Composable
fun desktopPreview(currentProject: ProjectState?) {
    var node: SmlNode? = if (currentProject?.isPageLoaded == true) currentProject.parsedPage else null
    val scrollState = rememberScrollState()
    var lang by remember { mutableStateOf("en") }
    val clickCount = remember { mutableStateOf(0) }

    if (node == null && currentProject != null) {
        // in case of syntax error we keep showing the last page
        node = currentProject.cachedPage
    }

    Column(modifier = Modifier.width(960.dp).height(560.dp).background(color = MaterialTheme.colors.primary)) {
        var expanded by remember { mutableStateOf(false) }
        val languages = currentProject?.getLanguages().orEmpty()

        Row {
            BasicText(
                text = "Desktop Preview",
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
                    painter = painterResource("drawable/portrait.xml"),
                    onClick = { currentProject?.isPortrait = true },
                    tooltipText = "Mobile Preview",
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
            // Outer phone box with fixed aspect ratio (16:9) that scales dynamically
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .size(960.dp, 540.dp )
                    .aspectRatio(16f/9f)
                    .clip(RoundedCornerShape(15.dp))
                    .background(Color(0xFF353739))
                    .border(2.dp, Color.Gray, RoundedCornerShape(15.dp))
            ) {
                // Inner screen with relative size
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .fillMaxHeight(0.96f)
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
                                    .size((1.0 / scale * 960.0).dp, (1.0 / scale * 540).dp)
                                    .background(pageBackgroundColor)

                            ) {
                                var modifier = Modifier as Modifier
                                //val scrollableProperty = node.properties["scrollable"] as? PropertyValue.StringValue
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
                                    .size((1.0 / scale * 960.0).dp, (1.0 / scale * 540).dp)
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