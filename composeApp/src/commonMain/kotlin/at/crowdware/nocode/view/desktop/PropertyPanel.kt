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
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocode.theme.ExtendedTheme
import at.crowdware.nocode.utils.*
import at.crowdware.nocode.viewmodel.ProjectState
import at.crowdware.nocode.viewmodel.listResourceFiles
import at.crowdware.nocode.viewmodel.loadTextFromResource
import java.io.File

@Composable
fun propertyPanel(modifier: Modifier,currentProject: ProjectState?) {
    Column(
        modifier = modifier.fillMaxHeight().background(color = MaterialTheme.colors.primary)
    ) {
        val nodes = remember {
            getAllElements()
        }

        BasicText(
            text = "Properties",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        val scrollState = rememberScrollState()
        val element = currentProject?.actualElement
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(end = 10.dp)
            ) {

                Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                    Column() {
                        if (element!= null && element.isNotEmpty()) {
                            Text(
                                text = element,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExtendedTheme.colors.syntaxColor
                            )
                            val content = loadTextFromResource("sml/${element}.sml")
                            val (parsedElement, error) = parseSML(content)
                            if (parsedElement != null) {
                                val description = getStringValue(parsedElement, "description", "")
                                val md = parseMarkdown(description)
                                Text(
                                    text = md,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colors.onPrimary
                                )


                                for (property in parsedElement.children) {
                                    if (property.name == "Property") {
                                        renderAnnotation(
                                            getStringValue(property, "name", ""),
                                            getStringValue(property, "description", "")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Available Elements",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExtendedTheme.colors.attributeValueColor
                            )

                            for (node in nodes) {
                                val elementName = getStringValue(node, "name", "")
                                for (child in node.children) {
                                    if (child.name == "AllowedRoot") {
                                        val name = getStringValue(child, "name", "")
                                        if (name == element) {
                                            Text(
                                                text = elementName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = MaterialTheme.colors.onPrimary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

fun getAllElements(): List<SmlNode> {
    val list = mutableListOf<SmlNode>()

    // 1. Laden aus Ressourcen (im JAR oder im Dev-Mode)
    val files = listResourceFiles("sml")
    for (file in files) {
        val content = loadTextFromResource("sml/$file")
        val (parsedElement, error) = parseSML(content)
        if (parsedElement != null) {
            list.add(parsedElement)
        }
    }

    // 2. Laden aus Plugin-Verzeichnissen
    val pluginCacheDir = File(System.getProperty("user.home"), "Library/Application Support/NoCodeDesigner/plugin-cache/")
    if (pluginCacheDir.exists() && pluginCacheDir.isDirectory) {
        pluginCacheDir.listFiles()?.forEach { pluginFolder ->
            if (pluginFolder.isDirectory) {
                val smlFolder = File(pluginFolder, "sml")
                if (smlFolder.exists() && smlFolder.isDirectory) {
                    smlFolder.listFiles { file -> file.isFile && file.extension == "sml" }?.forEach { smlFile ->
                        val content = smlFile.readText()
                        val (parsedElement, error) = parseSML(content)
                        if (parsedElement != null) {
                            list.add(parsedElement)
                        }
                    }
                }
            }
        }
    }

    return list
}

@Composable
fun renderAnnotation(name: String, description: String, isChildrenAnnotation: Boolean = false) {
    Spacer(modifier = Modifier.height(8.dp))
    if(isChildrenAnnotation) {
        Text(
            text = "Possible Children",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ExtendedTheme.colors.attributeNameColor)
    } else {
        Text(
            text = name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ExtendedTheme.colors.attributeNameColor
        )
    }
    val md = parseMarkdown(description)
    Text(
        text = md,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colors.onPrimary
    )
}
