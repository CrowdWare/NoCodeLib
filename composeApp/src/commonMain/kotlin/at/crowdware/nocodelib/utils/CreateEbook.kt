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

import at.crowdware.nocodelib.Version
import at.crowdware.nocodelib.viewmodel.GlobalAppState
import at.crowdware.nocodelib.viewmodel.GlobalProjectState
import at.crowdware.nocodelib.viewmodel.LicenseType
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import net.pwall.mustache.Template
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory


class CreateEbook {
    companion object {
        fun start(title: String, folder: String, source: String, book: Ebook) {
            val dir = File(folder)
            dir.mkdirs()

            val tempDir = createTempDirectory().toFile()
            val guid = UUID.randomUUID().toString()

            File(tempDir, "EPUB/parts").mkdirs()
            File(tempDir, "EPUB/images").mkdirs()
            File(tempDir, "EPUB/css").mkdirs()
            File(tempDir, "META-INF").mkdirs()

            copyAssets(book.theme, tempDir)
            copyImages(tempDir, source)
            writeContainer(tempDir)
            writeMimetype(tempDir)
            generatePackage(tempDir, book, guid)
            val toc = generateParts(tempDir, book, source)
            generateToc(tempDir, book, toc)

            val files = getAllFiles(tempDir)

            ZipOutputStream(Files.newOutputStream(Paths.get("$folder/$title.epub"))).use { zip ->
                files.forEach { file ->
                    zip.putNextEntry(ZipEntry(file.relativeTo(tempDir).path))
                    zip.write(file.readBytes())
                    zip.closeEntry()
                }
            }
            tempDir.deleteRecursively()
        }


        fun copyAssets(theme: String, targetDir: File) {
            val classLoader = Thread.currentThread().contextClassLoader
            val resourcePath = "themes/$theme/assets"
            val resourceURL = classLoader.getResource(resourcePath)
                ?: throw IllegalArgumentException("Resource not found: $resourcePath")

            copyDirectoryFromResources(classLoader, resourceURL, resourcePath, File(targetDir, "EPUB"))
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

        fun copyStreamToFile(inputStream: InputStream, targetFile: File) {
            targetFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        fun copyImages(dir: File, source: String) {
            val sourceDir = File(source, "images")
            val targetDir = File(dir, "EPUB/images")
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

        fun writeMimetype(dir: File) {
            val mimeFile = File(dir, "mimetype")
            mimeFile.writeText("application/epub+zip", Charsets.UTF_8)
        }

        fun writeContainer(dir: File) {
            val metaInfDir = File(dir, "META-INF")

            metaInfDir.mkdirs()

            val containerFile = File(metaInfDir, "container.xml")
            containerFile.writeText("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<container xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\" version=\"1.0\">\n" +
                    "  <rootfiles>\n" +
                    "    <rootfile full-path=\"EPUB/package.opf\" media-type=\"application/oebps-package+xml\"/>\n" +
                    "  </rootfiles>\n" +
                    "</container>", Charsets.UTF_8)
        }

        fun generatePackage(dir: File, book: Ebook, guid: String) {
            val context = mutableMapOf<String, Any>()

            context["uuid"] = guid
            context["lang"] = book.language
            context["title"] = book.name
            context["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            context["version"] = Version.version
            context["creator"] = book.creator
            context["creatorLink"] = book.creatorLink
            context["bookLink"] = book.bookLink
            context["license"] = book.license
            context["licenseLink"] = book.licenseLink
            context["generator"] = "FreeBookDesigner v." + Version.version
            context["license"] = "Non-commercial license"

            val items = mutableListOf<Map<String, String>>()
            val spine = mutableListOf<String>()

            for (part in book.parts) {
                if (!part.pdfOnly) {
                    val name = part.src.substringBefore(".").replace(" ", "-").lowercase()
                    if (name != "toc") {
                        val item = mutableMapOf<String, String>()
                        item["href"] = "parts/$name.xhtml"
                        item["id"] = name
                        item["type"] = "application/xhtml+xml"
                        items.add(item)
                        spine.add(name)
                    }
                }
            }

            val imagesDir = File(dir, "EPUB/images")
            if (imagesDir.exists()) {
                imagesDir.walkTopDown().forEach { file ->
                    if (file.isFile && file.name != ".DS_Store") {
                        val filename = file.nameWithoutExtension
                        val extension = file.extension
                        val item = mutableMapOf<String, String>()
                        item["href"] = "images/${file.name}"
                        item["id"] = "${filename}_img"
                        item["type"] = "image/$extension"
                        items.add(item)
                    }
                }
            }
            // Add items and spine to context
            context["items"] = items
            context["spine"] = spine

            // Read and process the template file
            val classLoader = Thread.currentThread().contextClassLoader

            // Path inside the resources (e.g., "themes/<theme>/assets")
            val resourcePath = "themes/${book.theme}/layout/package.opf"
            val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
            val data = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

            val template = Template.parse(data)
            val renderedXml = template.processToString(context)

            // Write the rendered XML to the output file
            val outputPath = Paths.get(dir.path, "EPUB", "package.opf")
            outputPath.parent.createDirectories()
            File(outputPath.toUri()).writeText(renderedXml, Charsets.UTF_8)
        }

        fun generateParts(dir: File, book: Ebook, source: String): List<Map<String, Any>> {
            val toc = mutableListOf<Map<String, Any>>()
            val item = mutableMapOf<String, Any>(
                "href" to "toc.xhtml",
                "name" to if (book.language == "de") "Inhaltsverzeichnis" else "Table of Contents",
                "id" to "nav",
                "parts" to mutableListOf<Any>()
            )
            toc.add(item)

            val path = Paths.get("").toAbsolutePath().toString()

            for (part in book.parts) {
                if (!part.pdfOnly) {
                    val context = mutableMapOf<String, Any>()
                    val partSourcePath = Paths.get(source, "parts", part.src).toFile()

                    val text = partSourcePath.readText(Charsets.UTF_8)
                    val name = part.src.substringBefore(".").replace(" ", "-").lowercase()

                    if (name != "toc") {
                        val options = MutableDataSet()
                        options.set(HtmlRenderer.GENERATE_HEADER_ID, true)
                        options.set(HtmlRenderer.RENDER_HEADER_ID,true)
                        // Tabellenunterstützung hinzufügen
                        options.set(Parser.EXTENSIONS, listOf(TablesExtension.create()))

                        val parser = Parser.builder(options).build()
                        val document = parser.parse(text)
                        val renderer = HtmlRenderer.builder(options).build()
                        // Markdown processing and table fixing
                        var html = fixImagePaths(fixTables(renderer.render(document)))

                        val linkList = getLinks(html, name)
                        toc.addAll(linkList)

                        context["content"] = html

                        val classLoader = Thread.currentThread().contextClassLoader
                        val resourcePath = "themes/${book.theme}/layout/template.xhtml"
                        val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
                        val templateData = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

                        val template = Template.parse(templateData)
                        val xhtml = template.processToString(context)

                        val outputFile = Paths.get(dir.path, "EPUB", "parts", "$name.xhtml").toFile()
                        outputFile.writeText(xhtml, Charsets.UTF_8)
                    }
                }
            }

            return toc
        }

        private fun fixImagePaths(input: String): String {
            return input.replace("src=\"", "src=\"../images/")
        }

        fun getAllFiles(dir: File): List<File> {
            return dir.walk().filter { it.isFile }.toList()
        }

        fun fixTables(text: String): String {
            return text
                .replace("<th align=\"center\"", "<th class=\"center\"")
                .replace("<th align=\"right\"", "<th class=\"right\"")
                .replace("<th align=\"left\"", "<th class=\"left\"")
                .replace("<td align=\"center\"", "<td class=\"center\"")
                .replace("<td align=\"right\"", "<td class=\"right\"")
                .replace("<td align=\"left\"", "<td class=\"left\"")
        }

        fun generateToc(dir: File, book: Ebook, parts: List<Map<String, Any>>) {
            val currentProject = GlobalProjectState.projectState
            val context = mutableMapOf<String, Any>()

            context["lang"] = book.language
            context["title"] = book.name
            context["date"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))
            context["version"] = Version.version
            context["creator"] = book.creator
            context["creatorLink"] = book.creatorLink
            context["bookLink"] = book.bookLink
            context["generator"] = "FreeBookDesigner v." + Version.version
            if (book.language == "de") {
                if (currentProject != null) {
                    context["publishedby"] = "Publiziert von"
                    context["publisher"] = GlobalAppState.appState?.license_publisher.toString()
                }
                context["licenseInformation"] = "Lizenzinformationen"
                context["from"] = "von"
                context["softwareLicense"] = "Software Lizenz"
                context["licenseTextA"] = "Dieses Buch wurde mit der"
                if (GlobalAppState.appState?.licenseType == LicenseType.FREE) {
                    context["isLicensedUnder"] = "ist lizenziert unter einer nicht-kommerziellen Lizenz."
                    context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
                    context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
                    context["licenseTextB"] = "nicht-kommerziellen Version"
                } else {
                    context["isLicensedUnder"] = "ist lizenziert unter einer kommerziellen Lizenz."
                    context["license"] = book.license
                    context["licenseLink"] = book.licenseLink
                    context["licenseTextB"] = "kommerziellen Version"
                }
                context["licenseTextC"] = "des"
                context["licenseTextD"] = " erstellt."
            } else {
                if (currentProject != null) {
                    context["publishedby"] = "Published by"
                    context["publisher"] = GlobalAppState.appState?.license_publisher.toString()
                }
                context["licenseInformation"] = "License information"
                context["from"] = "from"
                context["softwareLicense"] = "Software License"
                context["licenseTextA"] = "This book has been created with the"
                if (GlobalAppState.appState?.licenseType == LicenseType.FREE) {
                    context["isLicensedUnder"] = "is licensed under a non-commercial license."
                    context["license"] = "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International"
                    context["licenseLink"] = "https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1"
                    context["licenseTextB"] = "non-commercial version"
                } else {
                    context["isLicensedUnder"] = "is licensed under a commercial license."
                    context["license"] = book.license
                    context["licenseLink"] = book.licenseLink
                    context["licenseTextB"] = "commercial version"
                }
                context["licenseTextC"] = "of the"
                context["licenseTextD"] = "."
            }

            context["pageTitle"] = if (book.language == "de") "Inhaltsverzeichnis" else "Table of Contents"
            if (parts.size > 0)
                context["parts"] = parts

            val classLoader = Thread.currentThread().contextClassLoader
            val resourcePath = "themes/${book.theme}/layout/toc.xhtml"
            val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)
            val templateData = inputStream?.bufferedReader()?.use { it.readText() } ?: throw IllegalArgumentException("File not found: $resourcePath")

            val template = Template.parse(templateData)
            val xhtml = template.processToString(context)

            val outputPath = Paths.get(dir.path, "EPUB", "parts", "toc.xhtml")
            Files.writeString(outputPath, xhtml, StandardCharsets.UTF_8)
        }

        private fun getLinks(text: String, partName: String): List<Map<String, Any>> {
            val nodes = mutableListOf<Map<String, Any>>()
            val linksList = mutableListOf<Map<String, Any>>()

            for (line in text.split("\n")) {
                if (line.isBlank()) continue

                val c = when {
                    line.startsWith("<h1 ") -> 1
                    line.startsWith("<h2 ") -> 2
                    line.startsWith("<h3 ") -> 3
                    line.startsWith("<h4 ") -> 4
                    line.startsWith("<h5 ") -> 5
                    line.startsWith("<h6 ") -> 6
                    else -> 0
                }

                if (c > 0) {
                    val idStart = line.indexOf("id=") + 4
                    val idEnd = line.indexOf('"', idStart)
                    val id = line.substring(idStart, idEnd)

                    val nameStart = line.indexOf(">", idEnd) + 1
                    val nameEnd = line.indexOf("<", nameStart)
                    val name = line.substring(nameStart, nameEnd)

                    val item = mutableMapOf<String, Any>()
                    item["href"] = "$partName.xhtml#$id"
                    item["name"] = name
                    item["id"] = id
                    item["hasparts"] = false
                    item["parts"] = mutableListOf<Map<String, Any>>()

                    if (nodes.size < c) {
                        nodes.add(item)
                    } else {
                        nodes[c - 1] = item
                    }

                    if (c == 1) {
                        linksList.add(item)
                    } else {
                        (nodes[c - 2] as MutableMap<String, Any>)["hasparts"] = true
                        (nodes[c - 2]["parts"] as MutableList<Map<String, Any>>).add(item)
                    }
                }
            }

            return linksList
        }
    }
}