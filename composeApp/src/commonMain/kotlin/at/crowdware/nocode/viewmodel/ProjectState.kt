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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import at.crowdware.nocode.model.NodeType
import at.crowdware.nocode.model.TreeNode
import at.crowdware.nocode.utils.*
import com.darkrockstudios.libraries.mpfilepicker.MPFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.reflect.KClass


expect fun getNodeType(path: String): NodeType
expect suspend fun loadFileContent(path: String, uuid: String, pid: String): String
expect fun saveFileContent(path: String, uuid: String, pid: String, content: String)
expect fun createProjectState(): ProjectState
expect fun fileExists(path: String): Boolean
expect fun deleteFile(path: String)
expect fun createPage(path: String, title: String)
expect fun createPart(path: String)
expect fun renameFile(pathBefore: String, pathAfter: String)
expect fun copyAssetFile(path: String, target: String)
expect fun copyResourceToFile(resourcePath: String, outputPath: String)

enum class LicenseType {
    UNDEFINED,
    FREE,
    STARTER,
    PRO,
    EXPIRED
}

abstract class ProjectState {
    var currentFileContent by mutableStateOf(TextFieldValue(""))
    var fileName by mutableStateOf("")
    var folder by mutableStateOf("")
    var path by mutableStateOf("")
    var treeData by mutableStateOf<List<TreeNode>>(emptyList())
    var elementData by mutableStateOf<List<TreeNode>>(emptyList())
    var extension by mutableStateOf("")
        private set
    var isPageDialogVisible by mutableStateOf(false)
    var isPartDialogVisible by mutableStateOf(false)
    var isRenameFileDialogVisible by mutableStateOf(false)
    var isProjectStructureVisible by mutableStateOf(true)
    var isNewProjectDialogVisible by mutableStateOf(false)
    var isOpenProjectDialogVisible by mutableStateOf(false)
    var isImportImageDialogVisible by mutableStateOf(false)
    var isImportVideoDialogVisible by mutableStateOf(false)
    var isImportSoundDialogVisible by mutableStateOf(false)
    var isImportModelDialogVisible by mutableStateOf(false)
    var isImportTextureDialogVisible by mutableStateOf(false)
    var isCreateEbookVisible by mutableStateOf(false)
    var isCreateHTMLVisible by mutableStateOf(false)
    var isCreateCourseVisible by mutableStateOf(false)
    var isAboutDialogOpen by  mutableStateOf(false)
    var isEditorVisible by mutableStateOf(false)
    var currentTreeNode by mutableStateOf(null as TreeNode?)
    var isPageLoaded by mutableStateOf(false)
    var actualElement: KClass<*>? by mutableStateOf(null)
    var parseError: String? by mutableStateOf(null)
    var lang: String by mutableStateOf("")

    lateinit var pageNode: TreeNode
    lateinit var imagesNode: TreeNode
    lateinit var videosNode: TreeNode
    lateinit var soundsNode: TreeNode
    lateinit var partsNode: TreeNode
    lateinit var modelsNode: TreeNode
    lateinit var texturesNode: TreeNode
    var app: App? by mutableStateOf(null)
    var parsedPage: SmlNode? by mutableStateOf(null)
    var cachedPage: SmlNode? by mutableStateOf(null)

    //abstract fun loadSite()
    abstract fun loadApp()
    //abstract fun loadBook()
    abstract suspend fun loadProjectFiles(path: String, uuid: String, pid: String)
    abstract suspend fun createProjectFiles(
        path: String,
        uuid: String,
        pid: String,
        name: String,
        appId: String,
        theme: String,
        //createBook: Boolean,
        //createApp: Boolean,
        //createWebsite: Boolean,
        langs: List<String>
    )

    /*
    fun createEbook(title: String, folder: String, langs: List<String>, generator: String) {
        book?.let { CreateEbook.start(title, folder, this.folder, it, langs, generator = generator) }
    }
*/
    /*
    fun createHTML(folder: String) {
        site!!.deployDirHtml  = folder
        save(site!!)
        site?.let { CreateHTML.start(folder, this.folder, it) }
    }
*/
    /*
    fun createCourse(folder: String, lang: String) {
        site!!.deployDirHtml  = folder
        save(site!!)
        site?.let { CreateCourse.start(folder, this.folder, it, lang) }
    }
*/
    fun LoadProject(path: String = folder, uuid: String, pid: String) {
        folder = path
        println("loadProject: $folder")
        CoroutineScope(Dispatchers.Main).launch {
            loadProjectFiles(path, uuid, pid)
        }
    }

    fun ImportImageFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast(File.separator)
            val target = "${folder}/images/$filename"
            at.crowdware.nocode.viewmodel.copyAssetFile(file.path, target)
            println("copy: ${file.path} - $target")
            val pngTarget = if (!target.endsWith(".png")) {
                val pngPath = target.substringBeforeLast(".") + ".png"
                val tar = File(target)
                convertToPng(File(target), File(pngPath))
                tar.delete()
                pngPath
            } else {
                target
            }
            val node = TreeNode(title = mutableStateOf(pngTarget.substringAfterLast(File.separator)), path = pngTarget, type = getNodeType(
                pngTarget
            )
            )
            imagesNode.children.add(node)
        }
    }

    fun saveCourse(course: UIElement.Course): String {
        var sml = "\tCourse {\n"
        for (topic in course.topics) {
            sml += "\t\tTopic {label: \"${topic.label}\""
            topic.page?.let { sml += " page: \"$it\"" }
            if (topic.subtopics.isNotEmpty()) {
                sml += "\n"
                for (subtopic in topic.subtopics) {
                    sml += "\t\t\tSubtopic {label: \"${subtopic.label}\" id: \"${subtopic.id}\"}\n"
                }
                sml += "\t\t"
            }
            sml += "}\n"
        }
        sml += "\t}\n\n"
        return sml
    }

    /*
    fun save(site: Site) {
        val file = File(folder, "site.sml")
        var sml = "Site {\n"
        sml += "\tsmlVersion: \"${site.smlVersion}\"\n"
        sml += "\tname: \"${site.name}\"\n"
        sml += "\ttheme: \"${site.theme}\"\n"
        sml += "\tdescription: \"${site.description}\"\n"
        sml += "\tdeployDirHtml: \"${site.deployDirHtml}\"\n"
        sml += "\tauthor: \"${site.author}\"\n"
        sml += "\tauthorBio: \"${site.authorBio}\"\n"
        sml += "\n"
        if (site.course.topics.isNotEmpty()) {
            sml += saveCourse(site.course)
        }
        sml += "\n"
        sml += "\tTheme {\n"
        sml += "\t\tprimary: \"" + site.theme.primary.toString() + "\"\n"
        sml += "\t\tonPrimary: \"" + site.theme.onPrimary.toString() + "\"\n"
        sml += "\t\tprimaryContainer: \"" + site.theme.primaryContainer.toString() + "\"\n"
        sml += "\t\tonPrimaryContainer: \"" + site.theme.onPrimaryContainer.toString() + "\"\n"
        sml += "\t\tsecondary: \"" + site.theme.secondary.toString() + "\"\n"
        sml += "\t\tonSecondary: \"" + site.theme.onSecondary.toString() + "\"\n"
        sml += "\t\tsecondaryContainer: \"" + site.theme.secondaryContainer.toString() + "\"\n"
        sml += "\t\tonSecondaryContainer: \"" + site.theme.onSecondaryContainer.toString() + "\"\n"
        sml += "\t\ttertiary: \"" + site.theme.tertiary.toString() + "\"\n"
        sml += "\t\tonTertiary: \"" + site.theme.onTertiary.toString() + "\"\n"
        sml += "\t\ttertiaryContainer: \"" + site.theme.tertiaryContainer.toString() + "\"\n"
        sml += "\t\tonTertiaryContainer: \"" + site.theme.onTertiaryContainer.toString() + "\"\n"
        sml += "\t\terror: \"" + site.theme.error.toString() + "\"\n"
        sml += "\t\terrorContainer: \"" + site.theme.errorContainer.toString() + "\"\n"
        sml += "\t\tonError: \"" + site.theme.onError.toString() + "\"\n"
        sml += "\t\tonErrorContainer: \"" + site.theme.onErrorContainer.toString() + "\"\n"
        sml += "\t\tbackground: \"" + site.theme.background.toString() + "\"\n"
        sml += "\t\tonBackground: \"" + site.theme.onBackground.toString() + "\"\n"
        sml += "\t\tsurface: \"" + site.theme.surface.toString() + "\"\n"
        sml += "\t\tonSurface: \"" + site.theme.onSurface.toString() + "\"\n"
        sml += "\t\tsurfaceVariant: \"" + site.theme.surfaceVariant.toString() + "\"\n"
        sml += "\t\tonSurfaceVariant: \"" + site.theme.onSurfaceVariant.toString() + "\"\n"
        sml += "\t\toutline: \"" + site.theme.outline.toString() + "\"\n"
        sml += "\t\tinverseOnSurface: \"" + site.theme.inverseOnSurface.toString() + "\"\n"
        sml += "\t\tinverseSurface: \"" + site.theme.inverseSurface.toString() + "\"\n"
        sml += "\t\tinversePrimary: \"" + site.theme.inversePrimary.toString() + "\"\n"
        sml += "\t\tsurfaceTint: \"" + site.theme.surfaceTint.toString() + "\"\n"
        sml += "\t\toutlineVariant: \"" + site.theme.outlineVariant.toString() + "\"\n"
        sml += "\t\tscrim: \"" + site.theme.scrim.toString() + "\"\n"
        sml += "\t}\n"
        sml += "}\n"
        file.writeText(sml)
    }
*/
    /*
    fun save(app: App) {
        // TODO: Navigation is missing, but not used yet
        val file = File(folder, "app.sml")
        var sml = "App {\n"
        sml += "\tsmlVersion: \"${app.smlVersion}\"\n"
        sml += "\tname: \"${app.name}\"\n"
        sml += "\tdescription: \"${app.description}\"\n"
        sml += "\tid: \"${app.id}\"\n"
        sml += "\ticon: \"${app.icon}\"\n"
        sml += "\tauthor: \"${app.author}\"\n"
        sml += "\n"
        sml += "\tTheme {\n"
        sml += "\t\tprimary: \"" + app.theme.primary.toString() + "\"\n"
        sml += "\t\tonPrimary: \"" + app.theme.onPrimary.toString() + "\"\n"
        sml += "\t\tprimaryContainer: \"" + app.theme.primaryContainer.toString() + "\"\n"
        sml += "\t\tonPrimaryContainer: \"" + app.theme.onPrimaryContainer.toString() + "\"\n"
        sml += "\t\tsecondary: \"" + app.theme.secondary.toString() + "\"\n"
        sml += "\t\tonSecondary: \"" + app.theme.onSecondary.toString() + "\"\n"
        sml += "\t\tsecondaryContainer: \"" + app.theme.secondaryContainer.toString() + "\"\n"
        sml += "\t\tonSecondaryContainer: \"" + app.theme.onSecondaryContainer.toString() + "\"\n"
        sml += "\t\ttertiary: \"" + app.theme.tertiary.toString() + "\"\n"
        sml += "\t\tonTertiary: \"" + app.theme.onTertiary.toString() + "\"\n"
        sml += "\t\ttertiaryContainer: \"" + app.theme.tertiaryContainer.toString() + "\"\n"
        sml += "\t\tonTertiaryContainer: \"" + app.theme.onTertiaryContainer.toString() + "\"\n"
        sml += "\t\terror: \"" + app.theme.error.toString() + "\"\n"
        sml += "\t\terrorContainer: \"" + app.theme.errorContainer.toString() + "\"\n"
        sml += "\t\tonError: \"" + app.theme.onError.toString() + "\"\n"
        sml += "\t\tonErrorContainer: \"" + app.theme.onErrorContainer.toString() + "\"\n"
        sml += "\t\tbackground: \"" + app.theme.background.toString() + "\"\n"
        sml += "\t\tonBackground: \"" + app.theme.onBackground.toString() + "\"\n"
        sml += "\t\tsurface: \"" + app.theme.surface.toString() + "\"\n"
        sml += "\t\tonSurface: \"" + app.theme.onSurface.toString() + "\"\n"
        sml += "\t\tsurfaceVariant: \"" + app.theme.surfaceVariant.toString() + "\"\n"
        sml += "\t\tonSurfaceVariant: \"" + app.theme.onSurfaceVariant.toString() + "\"\n"
        sml += "\t\toutline: \"" + app.theme.outline.toString() + "\"\n"
        sml += "\t\tinverseOnSurface: \"" + app.theme.inverseOnSurface.toString() + "\"\n"
        sml += "\t\tinverseSurface: \"" + app.theme.inverseSurface.toString() + "\"\n"
        sml += "\t\tinversePrimary: \"" + app.theme.inversePrimary.toString() + "\"\n"
        sml += "\t\tsurfaceTint: \"" + app.theme.surfaceTint.toString() + "\"\n"
        sml += "\t\toutlineVariant: \"" + app.theme.outlineVariant.toString() + "\"\n"
        sml += "\t\tscrim: \"" + app.theme.scrim.toString() + "\"\n"
        sml += "\t}\n"
        sml += "}\n"
        file.writeText(sml)
    }*/

    fun ImportVideoFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast(File.separator)
            val target = "$folder${File.separator}videos${File.separator}$filename"
            at.crowdware.nocode.viewmodel.copyAssetFile(file.path, target)
            val node = TreeNode(title = mutableStateOf(filename), path = file.path, type = getNodeType(
                file.path
            )
            )
            videosNode.children.add(node)
        }
    }

    fun ImportSoundFile(list: List<MPFile<Any>>) {
        for (file in list) {
            val filename = file.path.substringAfterLast(File.separator)
            val target = "$folder${File.separator}sounds${File.separator}$filename"
            at.crowdware.nocode.viewmodel.copyAssetFile(file.path, target)
            val node = TreeNode(title = mutableStateOf(filename), path = file.path, type = getNodeType(
                file.path
            )
            )
            soundsNode.children.add(node)
        }
    }

    fun ImportModelFile(path: String) {
        val filename = path.substringAfterLast(File.separator)
        val target  = "$folder${File.separator}models${File.separator}$filename"
        at.crowdware.nocode.viewmodel.copyAssetFile(path, target)
        val node = TreeNode(title = mutableStateOf(filename), path = path, type = getNodeType(
            path
        )
        )
        modelsNode.children.add(node)
    }

    fun ImportTextureFile(path: String) {
        val filename = path.substringAfterLast(File.separator)
        val target  = "$folder${File.separator}textures${File.separator}$filename"
        at.crowdware.nocode.viewmodel.copyAssetFile(path, target)
        val node = TreeNode(title = mutableStateOf(filename), path = path, type = getNodeType(
            path
        )
        )
        texturesNode.children.add(node)
    }

    fun LoadFile(filePath: String) {
        path = filePath

        val regex = Regex("""/pages-([a-z]{2})/""")
        lang = regex.find(path)?.groupValues?.get(1) ?: ""

        CoroutineScope(Dispatchers.Main).launch {
            extension = path.substringAfterLast('.', "")
            if (extension.isEmpty()) {
                extension = when {
                    fileExists("$path.sml") -> "sml"
                    else -> {
                        println("Keine gültige Datei gefunden.")
                        return@launch // Wenn keine gültige Datei gefunden wird, breche ab
                    }
                }
                path = "$filePath.$extension"
            }
            var fileText = loadFileContent(path, "", "")
            fileText = fileText.replace("\t", "    ")
            if (extension == "sml") {

                val (smlNode, error) = parseSML(fileText)
                parsedPage = smlNode

                if (path.substringAfterLast(File.separator) == "app.sml") {
                    loadElementData(App())
                } else {
                    parseError = error
                    if (parsedPage != null) {
                        cachedPage = parsedPage
                        isPageLoaded = true
                        loadElementData(parsedPage)
                    }
                }
            } else {
                // TODO
                //page = Page(color = "", backgroundColor = "", title = "", padding = Padding(0, 0, 0, 0), scrollable =  "false", elements = mutableListOf())
                elementData = emptyList()
                val clsName = "at.crowdware.nocode.utils.Markdown"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }

            currentFileContent = TextFieldValue(
                text = fileText,
                selection = TextRange(fileText.length)
            )
            fileName = path.substringAfterLast(File.separator)
            isEditorVisible = true
        }
    }

    fun reloadPage() {
        if(extension == "sml" && fileName != "app.sml" && fileName != "ebook.sml") {
            val (smlNode, error) = parseSML(currentFileContent.text)
            parsedPage = smlNode
            parseError = error
            if (parsedPage != null) {
                cachedPage = parsedPage
                isPageLoaded = true
                loadElementData(parsedPage)
            }
        }
    }

    private fun loadElementData(obj: Any?) {
        // TODO
        /*
        when (obj) {
            is Page -> {
                elementData = listOf(mapPageToTreeNodes(page!!))
                val clsName = "at.crowdware.nocode.utils.Page"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
            is App -> {
                elementData = listOf(mapAppToTreeNode(obj as App))
                val clsName = "at.crowdware.nocode.utils.App"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }
            /*is Ebook -> {
                elementData = listOf(mapBookToTreeNode(obj as Ebook))
                val clsName = "at.crowdware.nocode.utils.Ebook"
                val clazz = Class.forName(clsName).kotlin
                actualElement = clazz
            }*/
        }*/
    }

    fun mapUIElementToTreeNode(uiElement: UIElement): TreeNode {
        // Create a TreeNode based on the type of the UIElement
        return when (uiElement) {
            is UIElement.TextElement -> TreeNode(
                title = mutableStateOf("Text"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.ButtonElement -> TreeNode(
                title = mutableStateOf("Button"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.ImageElement -> TreeNode(
                title = mutableStateOf("Image"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.AsyncImageElement -> TreeNode(
                title = mutableStateOf("AsyncImage"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.SpacerElement -> TreeNode(
                title = mutableStateOf("Spacer"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.VideoElement -> TreeNode(
                title = mutableStateOf("Video"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.YoutubeElement -> TreeNode(
                title = mutableStateOf("Youtube"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.SoundElement -> TreeNode(
                title = mutableStateOf("Sound"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.MarkdownElement -> TreeNode(
                title = mutableStateOf("Markdown"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.RowElement -> TreeNode(
                title = mutableStateOf("Row"),
                type = NodeType.DIRECTORY,
                path = "",
                children = mutableStateListOf(
                    *uiElement.uiElements.map { mapUIElementToTreeNode(it) }.toTypedArray()
                ),
                expanded = mutableStateOf(true)
            )
            is UIElement.ColumnElement -> TreeNode(
                title = mutableStateOf("Column"),
                type = NodeType.DIRECTORY,
                path = "",
                children = mutableStateListOf(
                    *uiElement.uiElements.map { mapUIElementToTreeNode(it) }.toTypedArray()
                ),
                expanded = mutableStateOf(true)
            )
            is UIElement.BoxElement -> TreeNode(
                title = mutableStateOf("Box"),
                type = NodeType.DIRECTORY,
                path = "",
                children = mutableStateListOf(
                    *uiElement.uiElements.map { mapUIElementToTreeNode(it) }.toTypedArray()
                ),
                expanded = mutableStateOf(true)
            )
            is UIElement.SceneElement -> TreeNode(
                title = mutableStateOf("Scene"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.Zero -> TreeNode(
                title = mutableStateOf("Zero Element"),
                type = NodeType.OTHER,
                path = "",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.Course ->  TreeNode(
                title = mutableStateOf("Course"),
                type = NodeType.OTHER,
                path ="",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.Topic ->  TreeNode(
                title = mutableStateOf("Topic"),
                type = NodeType.OTHER,
                path ="",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.Subtopic ->  TreeNode(
                title = mutableStateOf("Subtopic"),
                type = NodeType.OTHER,
                path ="",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.LazyColumnElement -> TreeNode(
                title = mutableStateOf("LazyColumn"),
                type = NodeType.OTHER,
                path ="",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
            is UIElement.LazyRowElement -> TreeNode(
                title = mutableStateOf("LazyRow"),
                type = NodeType.OTHER,
                path ="",
                children = mutableStateListOf(),
                expanded = mutableStateOf(false)
            )
        }
    }

    /*
    fun mapBookToTreeNode(book: Ebook): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("Ebook"),
            type = mutableStateOf("Ebook"),
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)
        )
        return rootNode
    }*/

    fun mapAppToTreeNode(app: App): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("App"),
            type = mutableStateOf("App"),
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)
        )
        return rootNode
    }

    fun mapPageToTreeNodes(page: Page): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("Page"),
            type = NodeType.DIRECTORY,
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)  // Root is expanded by default
        )
        rootNode.children.addAll(page.elements.map { mapUIElementToTreeNode(it) })

        return rootNode
    }

    fun saveFileContent() {
        if (path.isEmpty()) return
        at.crowdware.nocode.viewmodel.saveFileContent(path, "", "", currentFileContent.text)
    }

    fun addPage(name: String, currentTreeNode: TreeNode?) {
        val path = "${currentTreeNode?.path}${File.separator}$name.sml"
        at.crowdware.nocode.viewmodel.createPage(path, name)

        val newNode = TreeNode(title = mutableStateOf( "${name}.sml"), path = path, type= NodeType.SML)
        val updatedChildren = pageNode.children + newNode
        pageNode.children.clear()
        pageNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun addPart(name: String, currentTreeNode: TreeNode?) {
        val path = "${currentTreeNode?.path}${File.separator}$name.md"
        at.crowdware.nocode.viewmodel.createPart(path)

        val newNode = TreeNode(title = mutableStateOf( "${name}.md"), path = path, type= NodeType.MD)
        val updatedChildren = partsNode.children + newNode
        partsNode.children.clear()
        partsNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun deleteItem(treeNode: TreeNode) {
        at.crowdware.nocode.viewmodel.deleteFile(treeNode.path)

        if (treeNode.type == NodeType.SML) {
            val title = treeNode.title.value

            println("before: ${pageNode.children.size}")
            println("node: ${pageNode.title.value}")
            pageNode.children.remove(treeNode)
            println("after: ${pageNode.children.size}")

            if (title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }
        } else if (treeNode.type == NodeType.MD) {
            val title = treeNode.title.value

            partsNode.children.remove(treeNode)

            if (title == fileName) {
                // we have to remove the editor, because file cannot be edited anymore
                currentFileContent = TextFieldValue("")
                path = ""
                fileName = ""
                extension = ""
                isEditorVisible = false
            }
        } else {
            imagesNode.children.remove(treeNode)
        }
    }

    fun renameFile(name: String) {
        val folder = currentTreeNode?.path?.substringBeforeLast(File.separator)
        val ext = currentTreeNode?.path?.substringAfterLast(".")
        val newPath = "$folder${File.separator}$name.$ext"
        at.crowdware.nocode.viewmodel.renameFile(currentTreeNode?.path!!, newPath)
        currentTreeNode!!.title.value = "$name.$ext"
        currentTreeNode!!.path = newPath
    }

    fun convertToPng(inputFile: File, outputFile: File) {
        val image: BufferedImage = ImageIO.read(inputFile)
        ImageIO.write(image, "png", outputFile)
    }
}

object GlobalProjectState {
    var projectState: ProjectState? = null
}