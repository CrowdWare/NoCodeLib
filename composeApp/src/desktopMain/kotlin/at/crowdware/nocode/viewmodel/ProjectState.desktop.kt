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

package at.crowdware.nocode.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import at.crowdware.nocode.model.NodeType
import at.crowdware.nocode.model.TreeNode
import at.crowdware.nocode.model.extensionToNodeType
import at.crowdware.nocode.utils.fillAppFromSmlNode
import at.crowdware.nocode.utils.parseSML
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarFile

actual fun getNodeType(path: String): NodeType {
    val file = File(path)
    return when {
        file.isDirectory -> NodeType.DIRECTORY
        else -> extensionToNodeType[file.extension.lowercase()] ?: NodeType.OTHER
    }
}

actual suspend fun loadFileContent(path: String, uuid: String, pid: String): String {
    val file = File(path)
    return try {
        if(!file.exists())
            return ""
        file.readText()
    } catch (e: IOException) {
        throw IOException("Error reading file: ${e.message}", e)
    }
}

actual fun saveFileContent(path: String, uuid: String, pid: String, content: String) {
    val file = File(path)
    try {
        file.writeText(content)
        file.setLastModified(System.currentTimeMillis())
    } catch (e: IOException) {
        throw IOException("Error writing to file: ${e.message}", e)
    }
}

actual fun loadTextFromResource(fileName: String): String? {
    return try {
        object {}.javaClass.getResource("/$fileName")?.readText()
    } catch (e: Exception) {
        null
    }
}

actual fun listResourceFiles(path: String): List<String> {
    val resource = object {}.javaClass.getResource("/$path") ?: return emptyList()

    return when (resource.protocol) {
        "file" -> {
            File(resource.toURI()).listFiles()?.map { it.name } ?: emptyList()
        }
        "jar" -> {
            val jarPath = resource.path.substringBefore("!").removePrefix("file:")
            val jarFile = JarFile(URLDecoder.decode(jarPath, "UTF-8"))
            val entries = jarFile.entries()
            val result = mutableListOf<String>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val name = entry.name
                if (name.startsWith(path) && !entry.isDirectory) {
                    val relativeName = name.removePrefix("$path/") // Nur Dateiname ohne Pfad
                    if (!relativeName.contains("/")) { // Keine Subfolder
                        result.add(relativeName)
                    }
                }
            }
            result
        }
        else -> emptyList()
    }
}

class DesktopProjectState : ProjectState() {
    override suspend fun loadProjectFiles(path: String, uuid: String, pid: String) {
        val file = File(path)

        fun getNodeType(file: File): NodeType {
            return if (file.isDirectory) {
                NodeType.DIRECTORY
            } else {
                val extension = file.extension.lowercase()
                extensionToNodeType[extension] ?: NodeType.OTHER
            }
        }

        fun mapFileToTreeNode(file: File): TreeNode {
            val allowedFolderNames = listOf(
                "images",
                "videos",
                "sounds",
                "models",
                "pages",
                "parts",
                "data",
                "translations"
            )
            val nodeType = getNodeType(file)
            val children = if (file.isDirectory) {
                file.listFiles()
                    ?.filter { it.name != ".DS_Store" }
                    ?.flatMap {
                        if (it.isDirectory && allowedFolderNames.contains(it.name)) {
                            it.listFiles()?.filter { file -> file.name != ".DS_Store" }?.map { mapFileToTreeNode(it) }
                                ?: emptyList()
                        } else if (!it.isDirectory) {
                            listOf(mapFileToTreeNode(it))
                        } else {
                            emptyList()
                        }
                    } ?: emptyList()
            } else {
                emptyList()
            }
            val statefulChildren = SnapshotStateList<TreeNode>().apply {
                addAll(children)
            }
            val node = TreeNode(
                title = mutableStateOf(file.name),
                path = file.path,
                type = nodeType,
                children = statefulChildren
            )
            if (node.title.value == "pages") {
                pageNode = node
            } else if (node.title.value == "images") {
                imagesNode = node
            } else if (node.title.value == "videos") {
                videosNode = node
            } else if (node.title.value == "sounds") {
                soundsNode = node
            } else if (node.title.value == "parts") {
                partsNode = node
            } else if (node.title.value == "models") {
                modelsNode = node
            } else if (node.title.value == "textures") {
                texturesNode = node
            } else if (node.title.value == "translations") {
                translationsNode = node
            } else if (node.title.value == "data") {
                dataNode = node
            }
            return node
        }

        val nodes = file.listFiles()
            // Python 3 server.py runs the webserver for NoCodeBrowser testing
            ?.filter {
                it.name != ".DS_Store" &&
                        !it.name.endsWith(".py") &&
                        (it.isDirectory && it.name in listOf(
                            "images",
                            "sounds",
                            "videos",
                            "models",
                            "textures",
                            "translations",
                            "pages",
                            "parts",
                            "data"
                        )) ||
                        (it.isFile && it.name in listOf("app.sml"))
            }
            ?.map { mapFileToTreeNode(it) }
            ?: emptyList()

        val sortedNodes = nodes.sortedWith(
            compareBy<TreeNode> { it.type != NodeType.DIRECTORY }
                .thenBy { it.title.value }
        )

        for (node in sortedNodes) {
            val sortedChildren = node.children.sortedWith(
                compareBy<TreeNode> { it.type != NodeType.DIRECTORY }
                    .thenBy { it.title.value }
            )

            node.children.clear()
            node.children.addAll(sortedChildren)
        }

        treeData = sortedNodes.toList()
        folder = path

        // app.sml load and parse
        val appFile = File("$folder/app.sml")
        if (appFile.exists()) {
            loadApp()

            val langDir = File(folder, "pages")
            val homeFile = File(langDir, "home.sml")
            if (homeFile.exists()) {
                LoadFile("$folder/pages/home.sml")
            }
        }
    }

    override fun loadApp() {
        val appFile = File("$folder/app.sml")
        try {
            val uiSml = appFile.readText()
            val (parsed, error) = parseSML(uiSml)
            app = parsed?.let { fillAppFromSmlNode(it) }
            parsedApp = parsed

            loadDatasources()
        } catch (e: Exception) {
            println("Error parsing app.sml: ${e.message}")
        }
    }

    override suspend fun createProjectFiles(
        path: String,
        uuid: String,
        pid: String,
        name: String,
        appId: String,
        theme: String
    ) {
        val dir = File("$path$name")
        dir.mkdirs()
        val pages = File("$path$name/pages")
        pages.mkdirs()
        val videos = File("$path$name/videos")
        videos.mkdirs()
        val sounds = File("$path$name/sounds")
        sounds.mkdirs()
        val images = File("$path$name/images")
        images.mkdirs()
        val models = File("$path$name/models")
        models.mkdirs()
        val textures = File("$path$name/textures")
        textures.mkdirs()
        val translations = File("$path$name/translations")
        translations.mkdirs()
        val data = File("$path$name/data")
        data.mkdirs()
        createParts(path, name)
        val app = File("$path$name/app.sml")
        var appContent = """
                App {
                    smlVersion: "1.1"
                    name: "$name"
                    version: "1.0"
                    id: "$appId.$name"
                    icon: "icon.png"

                """.trimIndent()

        appContent += if (theme == "Light")
            writeLightTheme()
        else
            writeDarkTheme()
        appContent += "// deployment start - don't edit here\n\n// deployment end\n}\n\n"
        app.writeText(appContent)
        createPages(path, name)
        copyResourceToFile("python/server.py", "$path/$name/server.py")
        copyResourceToFile("python/upd_deploy.py", "$path/$name/upd_deploy.py")
        copyResourceToFile("icons/default.icon.png", "$path/$name/images/icon.png")

        val imageFiles = File("$path$name/images")
        imageFiles.mkdirs()

        LoadProject("$path$name", uuid, pid)
    }
}

fun createParts( path: String, name: String) {
    val parts = File("$path$name/parts")
    parts.mkdirs()
    val homemd = File("$path$name/parts/home.md")
    homemd.writeText("# Headline\nLorem ipsum dolor\n")
}

fun createPages(path: String, name: String) {

    val home = File("$path$name/pages/home.sml")

    val homeContent = """
            Page {
                padding: "8"

                Column {
                    padding: "8"

                    Markdown { text: "# Welcome" }
                }
            }
            """.trimIndent()
    home.writeText(homeContent)
}

fun writeDarkTheme(): String {
    var content = "\n"
    content += "\tTheme {\n"
    content += "\t\tprimary: \"#FFB951\"\n"
    content += "\t\tonPrimary: \"#452B00\"\n"
    content += "\t\tprimaryContainer: \"#633F00\"\n"
    content += "\t\tonPrimaryContainer: \"#FFDDB3\"\n"
    content += "\t\tsecondary: \"#DDC2A1\"\n"
    content += "\t\tonSecondary: \"#3E2D16\"\n"
    content += "\t\tsecondaryContainer: \"#56442A\"\n"
    content += "\t\tonSecondaryContainer: \"#FBDEBC\"\n"
    content += "\t\ttertiary: \"#B8CEA1\"\n"
    content += "\t\tonTertiary: \"#243515\"\n"
    content += "\t\ttertiaryContainer: \"#3A4C2A\"\n"
    content += "\t\tonTertiaryContainer: \"#D4EABB\"\n"
    content += "\t\terror: \"#FFB4AB\"\n"
    content += "\t\terrorContainer: \"#93000A\"\n"
    content += "\t\tonError: \"#690005\"\n"
    content += "\t\tonErrorContainer: \"#FFDAD6\"\n"
    content += "\t\tbackground: \"#1F1B16\"\n"
    content += "\t\tonBackground: \"#EAE1D9\"\n"
    content += "\t\tsurface: \"#1F1B16\"\n"
    content += "\t\tonSurface: \"#EAE1D9\"\n"
    content += "\t\tsurfaceVariant: \"#4F4539\"\n"
    content += "\t\tonSurfaceVariant: \"#D3C4B4\"\n"
    content += "\t\toutline: \"#9C8F80\"\n"
    content += "\t\tinverseOnSurface: \"#1F1B16\"\n"
    content += "\t\tinverseSurface: \"#EAE1D9\"\n"
    content += "\t\tinversePrimary: \"#825500\"\n"
    content += "\t\tsurfaceTint: \"#FFB951\"\n"
    content += "\t\toutlineVariant: \"#4F4539\"\n"
    content += "\t\tscrim: \"#000000\"\n"
    content += "\t}\n\n"
    return content
}

fun writeLightTheme(): String {
    var content = "\n"
    content += "\tTheme {\n"
    content += "\t\tprimary: \"#825500\"\n"
    content += "\t\tonPrimary: \"#FFFFFF\"\n"
    content += "\t\tprimaryContainer: \"#FFDDB3\"\n"
    content += "\t\tonPrimaryContainer: \"#291800\"\n"
    content += "\t\tsecondary: \"#6F5B40\"\n"
    content += "\t\tonSecondary: \"#FFFFFF\"\n"
    content += "\t\tsecondaryContainer: \"#FBDEBC\"\n"
    content += "\t\tonSecondaryContainer: \"#271904\"\n"
    content += "\t\ttertiary: \"#51643F\"\n"
    content += "\t\tonTertiary: \"#FFFFFF\"\n"
    content += "\t\ttertiaryContainer: \"#D4EABB\"\n"
    content += "\t\tonTertiaryContainer: \"#102004\"\n"
    content += "\t\terror: \"#BA1A1A\"\n"
    content += "\t\terrorContainer: \"#FFDAD6\"\n"
    content += "\t\tonError: \"#FFFFFF\"\n"
    content += "\t\tonErrorContainer: \"#410002\"\n"
    content += "\t\tbackground: \"#FFFBFF\"\n"
    content += "\t\tonBackground: \"#1F1B16\"\n"
    content += "\t\tsurface: \"#FFFBFF\"\n"
    content += "\t\tonSurface: \"#1F1B16\"\n"
    content += "\t\tsurfaceVariant: \"#F0E0CF\"\n"
    content += "\t\tonSurfaceVariant: \"#4F4539\"\n"
    content += "\t\toutline: \"#817567\"\n"
    content += "\t\tinverseOnSurface: \"#F9EFE7\"\n"
    content += "\t\tinverseSurface: \"#34302A\"\n"
    content += "\t\tinversePrimary: \"#FFB951\"\n"
    content += "\t\tsurfaceTint: \"#825500\"\n"
    content += "\t\tutlineVariant: \"#D3C4B4\"\n"
    content += "\t\tscrim: \"#000000\"\n"
    content += "\t}\n\n"
    return content
}

actual fun createProjectState(): ProjectState {
    return DesktopProjectState()
}

actual fun copyResourceToFile(resourcePath: String, outputPath: String) {
    println("copyResourceToFile: $resourcePath, $outputPath")
    val classLoader = Thread.currentThread().contextClassLoader
    val inputStream: InputStream? = classLoader.getResourceAsStream(resourcePath)

    if (inputStream != null) {
        val targetPath = Paths.get(outputPath)
        Files.createDirectories(targetPath.parent)
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)
    } else {
        println("Ressource $resourcePath could not be found.")
    }
}

actual fun fileExists(path: String): Boolean {
    return File(path).exists()
}

actual fun deleteFile(path: String) {
    File(path).delete()
}

actual fun createPage(path: String, title: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("Page {\n\ttitle:\"$title\"\n}")
}

actual fun createPart(path: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("# Header\nLorem ipsum dolor\n")
}

actual fun createData(path: String) {
    val file = File(path)
    file.createNewFile()
    file.writeText("{\n    \"Property\":\"value\"\n}\n")
}

actual fun renameFile(pathBefore: String, pathAfter: String) {
    File(pathBefore).renameTo(File(pathAfter))
}

actual fun copyAssetFile(path: String, target: String) {
    val sourceFile = File(path)
    val targetFile = File(target)
    sourceFile.copyTo(targetFile, overwrite = true)
}
