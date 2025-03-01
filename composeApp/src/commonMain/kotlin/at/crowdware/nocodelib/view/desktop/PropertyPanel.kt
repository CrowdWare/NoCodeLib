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

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import at.crowdware.nocodelib.theme.ExtendedTheme
import at.crowdware.nocodelib.utils.*
import at.crowdware.nocodelib.viewmodel.ProjectState
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

@Composable
fun propertyPanel(currentProject: ProjectState?) {
    Column(
        modifier = Modifier.width(320.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary)
    ) {
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
                        if (element != null) {
                            element.simpleName?.let {
                                Text(
                                    text = it.substringBefore("Element"),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.syntaxColor
                                )
                            }

                            element.members.forEach { member ->
                                if (member is KProperty<*>) {
                                    if (member.annotations.any { it is IgnoreForDocumentation }) {
                                        return@forEach
                                    }

                                    member.annotations.forEach { annotation ->
                                        when (annotation) {
                                            is WeightAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is HexColorAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is PaddingAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is MarkdownAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is IntAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is StringAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                            is LinkAnnotation -> {
                                                renderAnnotation(member.name, annotation.description)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (element != null) {
                    if (element.simpleName == "App") {
                        Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                            Column() {

                                Text(
                                    text = "Theme",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.syntaxColor
                                )
                                renderAnnotation("colorName", "A definition of all available colors. The colors are entered as hex values.\nSample:\nTheme {\n\tprimary: \"#825500\"\n}")
                            }
                        }
                    }
                    else if (element.simpleName == "Ebook") {
                        Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                            Column() {

                                Text(
                                    text = "Part",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.syntaxColor
                                )
                                val md = parseMarkdown("The list of the parts will also organise the order in the ebook.")
                                Text(
                                    text = md,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colors.onPrimary
                                )
                                renderAnnotation("src", "A part of the ebook.\nSample:\nPart {\n\tsrc: \"home.md\"\n}\n\nPart {\n" +
                                        "\tsrc: \"second.md\"\n" +
                                        "}")
                            }
                        }
                    }
                    else if (element.simpleName == "Page" || element.simpleName == "ColumnElement" || element.simpleName == "RowElement") {

                        Row(modifier = Modifier.background(MaterialTheme.colors.primary).fillMaxWidth().padding(8.dp)) {
                            Column() {
                                Text(
                                    text = "Available Elements",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExtendedTheme.colors.syntaxColor
                                )


                                val sealedClass: KClass<UIElement> = UIElement::class
                                val subclasses = sealedClass.sealedSubclasses

                                subclasses.forEach { subclass ->
                                    subclass.simpleName?.let {
                                        if (it != "Zero") {
                                            var clsName = ""
                                            if (it == "Page") {
                                                clsName = "at.crowdware.nocodelib.utils.Page"
                                            } else {
                                                clsName = "at.crowdware.nocodelib.utils.UIElement\$${it}"
                                            }
                                            val clazz = Class.forName(clsName).kotlin
                                            // Retrieve the MyCustomAnnotation from the class
                                            val annotation = clazz.findAnnotation<ElementAnnotation>()
                                            Text(
                                                text = it.substringBefore("Element"),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ExtendedTheme.colors.attributeNameColor
                                            )
                                            if (annotation != null) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                val md = parseMarkdown(annotation.description)
                                                Text(
                                                    text = md,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = MaterialTheme.colors.onPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun renderAnnotation(name: String, description: String) {
    Text(
        text = name,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ExtendedTheme.colors.attributeNameColor
    )
    val md = parseMarkdown(description)
    Text(
        text = md,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colors.onPrimary
    )
}
