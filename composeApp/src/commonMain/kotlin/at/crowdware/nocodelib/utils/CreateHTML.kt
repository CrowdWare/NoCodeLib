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

package at.crowdware.nocodelib.utils

import at.crowdware.nocodelib.utils.CreateEbook.Companion.copyStreamToFile
import at.crowdware.nocodelib.utils.UIElement.*
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import net.pwall.mustache.Template
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CreateHTML {
    companion object {
        var dir = File("")
        var sourceDir = File("")

        fun start(folder: String, source: String, app: App) {
            dir = File(folder)
            sourceDir = File(source)
            val assets = File(dir, "assets")
            assets.mkdirs()

            copyAssets(assets)
            copyImages(dir, source)

            // create a html file for all pages
            val sourceDir = File(source, "pages")
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val page = parsePage(file.readText())
                    if (page.first != null) {
                        val name = file.name.substringBeforeLast(".sml")
                        val html = getHtmlContent(page.first!!)
                        val context = mutableMapOf<String, Any>()

                        println("desc: ${app.description}")
                        context["name"] = app.name
                        context["description"] = app.description
                        context["title"] = app.name + " - " + page.first!!.title
                        context["content"] = html

                        val classLoader = Thread.currentThread().contextClassLoader
                        val resourcePath = "templates/page.html"
                        val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
                        val templateData = inputStream?.bufferedReader()?.use { it.readText() }
                            ?: throw IllegalArgumentException("File not found: $resourcePath")

                        val template = Template.parse(templateData)
                        val xhtml = template.processToString(context)

                        val outputFile = Paths.get(dir.path, "$name.html").toFile()
                        outputFile.writeText(xhtml, Charsets.UTF_8)
                    }
                }
            }
        }

        fun copyImages(dir: File, source: String) {
            val sourceDir = File(source, "images")
            val targetDir = File(dir, "assets/images")
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val targetFile = File(targetDir, file.name)
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
        }

        fun copyAssets(targetDir: File) {
            val classLoader = Thread.currentThread().contextClassLoader
            val resourcePath = "templates/assets"
            val resourceURL = classLoader.getResource(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

            copyDirectoryFromResources(classLoader, resourceURL, resourcePath, targetDir)
        }

        fun copyDirectoryFromResources(classLoader: ClassLoader, resourceURL: URL, resourcePath: String, targetDir: File) {
            if (resourceURL.protocol == "jar") {
                val jarPath = resourceURL.path.substringBefore("!")
                val jarFile = File(URL(jarPath).toURI())
                val jar = java.util.jar.JarFile(jarFile)

                jar.entries().asSequence().filter { entry ->
                    entry.name.startsWith(resourcePath) && !entry.isDirectory
                }.forEach { entry ->
                    val entryName = entry.name.removePrefix(resourcePath).trimStart('/')
                    val targetFile = File(targetDir, entryName)

                    if (!targetFile.parentFile.exists()) {
                        targetFile.parentFile.mkdirs() // Ensure parent directories exist
                    }

                    classLoader.getResourceAsStream(entry.name)?.use { inputStream ->
                        copyStreamToFile(inputStream, targetFile)
                    }
                }
            } else {
                val directory = File(resourceURL.toURI())

                directory.walkTopDown().forEach { file ->
                    val relativePath = file.relativeTo(directory).path
                    val targetFile = File(targetDir, relativePath)

                    if (file.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        classLoader.getResourceAsStream("$resourcePath/$relativePath")?.use { inputStream ->
                            copyStreamToFile(inputStream, targetFile)
                        }
                    }
                }
            }
        }

        fun getHtmlContent(page: Page): String {
            var html = ""

            html += "<section class=\"container\">\n"
            html += "<div class=\"row\">\n"
            html += "<div class=\"col-md-12\">\n"
            for (element in page.elements) {
                html += getHtmlFromElement(element)
            }
            html += "</div>\n"
            html += "</div>\n"
            html += "</section>\n"
            return html
        }

        fun getHtmlFromElement(element: UIElement): String {
            var html = ""
            when (element) {
                is TextElement -> {
                    html += getHtmlFromText(element)
                }
                is MarkdownElement -> {
                    html += getHtmlFromMarkdown(element)
                }
                is ColumnElement -> {
                    html += getHtmlFromColumn(element)
                }
                is RowElement -> {
                    html += getHtmlFromRow(element)
                }
                is ButtonElement -> {
                    html += getHtmlFromButton(element)
                }
                is SpacerElement -> {
                    html += "<div style=\"margin-top: 20px;\"></div>\n"
                }
                is ImageElement -> {
                    html += "<img class=\"img-fluid\" src=\"assets/images/${element.src}\"/>\n"

                }
                is YoutubeElement -> {
                    html += "<iframe width=\"333\" height=\"197\" src=\"https://www.youtube.com/embed/${element.id}\" \n" +
                            "title=\"YouTube video player\" \n" +
                            "frameborder=\"0\" \n" +
                            "allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" \n" +
                            "referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen>\n" +
                            "</iframe>"
                }
                else -> {
                    // ignore for now
                }
            }
            return html
        }

        fun getHtmlFromText(element: TextElement): String {
            var html = ""
            html += "<p>" + element.text + "\n</p>"
            return html
        }

        fun getHtmlFromMarkdown(element: MarkdownElement): String {
            val options = MutableDataSet()
            options.set(HtmlRenderer.GENERATE_HEADER_ID, true)
            options.set(HtmlRenderer.RENDER_HEADER_ID,true)
            var text = ""
            if (element.part.isNotEmpty()) {
                text = File(sourceDir, "parts/" + element.part).readText()
            } else {
                text = element.text
            }
            val parser = Parser.builder(options).build()
            val document = parser.parse(text)
            val renderer = HtmlRenderer.builder(options).build()
            return renderer.render(document)
        }

        fun getHtmlFromColumn(element: ColumnElement): String {
            var html = ""
            for (ele in element.uiElements) {
                html += getHtmlFromElement(ele)
            }
            return html
        }

        fun getHtmlFromRow(element: RowElement): String {
            var html = ""
            for (ele in element.uiElements) {
                html += getHtmlFromElement(ele)
            }
            return html
        }

        fun getHtmlFromButton(element: ButtonElement): String {
            val link = element.link.substringAfter(":")
            return "<a target=\"_blank\" href=\"$link\" class=\"btn btn-primary w-100 mt-3\">${element.label}</a>\n"
        }
    }
}