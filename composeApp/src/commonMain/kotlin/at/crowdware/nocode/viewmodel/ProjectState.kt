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
expect fun loadTextFromResource(fileName: String): String

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
    var isAboutDialogOpen by  mutableStateOf(false)
    var isEditorVisible by mutableStateOf(false)
    var currentTreeNode by mutableStateOf(null as TreeNode?)
    var isPageLoaded by mutableStateOf(false)
    var actualElement: String by mutableStateOf("")
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

    abstract fun loadApp()
    abstract suspend fun loadProjectFiles(path: String, uuid: String, pid: String)
    abstract suspend fun createProjectFiles(
        path: String,
        uuid: String,
        pid: String,
        name: String,
        appId: String,
        theme: String,
        langs: List<String>
    )

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
                    val properties = mutableMapOf<String, PropertyValue>()
                    val children = mutableListOf<SmlNode>()
                    properties.put("smlVersion", PropertyValue.StringValue("1.1"))
                    properties.put("name", PropertyValue.StringValue(""))
                    properties.put("description", PropertyValue.StringValue(""))
                    properties.put("id", PropertyValue.StringValue("com.example.appname"))
                    properties.put("icon", PropertyValue.StringValue("icon.png"))
                    loadElementData(SmlNode("App", properties, children))
                } else {
                    parseError = error
                    if (parsedPage != null) {
                        cachedPage = parsedPage
                        isPageLoaded = true
                        loadElementData(parsedPage)
                    }
                }
            } else {
                val rootNode = TreeNode(
                    title = mutableStateOf("Markdown"),
                    type = NodeType.OTHER,
                    path = "",
                    children = mutableStateListOf(),
                    expanded = mutableStateOf(false)
                )
                elementData = listOf(rootNode)
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

    private fun loadElementData(node: SmlNode?) {
        actualElement = node?.name!!
        when (node.name) {
            "Page" -> {
                elementData = listOf(mapPageToTreeNodes(node))
            }
            "App" -> {
                elementData = listOf(mapAppToTreeNode(node))
            } else ->{
                println("loadElementData: ${node.name} not implemented")
            }
        }
    }

    fun mapAppToTreeNode(node: SmlNode): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("App"),
            type = mutableStateOf("App"),
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)
        )
        return rootNode
    }

    fun mapPageToTreeNodes(node: SmlNode): TreeNode {
        val rootNode = TreeNode(
            title = mutableStateOf("Page"),
            type = NodeType.DIRECTORY,
            path = "",
            children = mutableStateListOf(),
            expanded = mutableStateOf(true)  // Root is expanded by default
        )
        mapSmlNodeToTreeItem(rootNode, node)
        return rootNode
    }

    fun mapSmlNodeToTreeItem(root: TreeNode, node: SmlNode) {
        node.children.forEach { child ->
            val treeNode = TreeNode(
                title = mutableStateOf(child.name),
                type = if (child.children.isNotEmpty()) NodeType.DIRECTORY else NodeType.OTHER,
                children = mutableStateListOf(),
                expanded = mutableStateOf(true)
            )
            root.children.add(treeNode)
            mapSmlNodeToTreeItem(treeNode, child)
        }
    }

    fun saveFileContent() {
        if (path.isEmpty())
            return
        saveFileContent(path, "", "", currentFileContent.text)
    }

    fun addPage(name: String, currentTreeNode: TreeNode?) {
        val path = "${currentTreeNode?.path}${File.separator}$name.sml"
        createPage(path, name)

        val newNode = TreeNode(title = mutableStateOf( "${name}.sml"), path = path, type= NodeType.SML)
        val updatedChildren = pageNode.children + newNode
        pageNode.children.clear()
        pageNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun addPart(name: String, currentTreeNode: TreeNode?) {
        val path = "${currentTreeNode?.path}${File.separator}$name.md"
        createPart(path)

        val newNode = TreeNode(title = mutableStateOf( "${name}.md"), path = path, type= NodeType.MD)
        val updatedChildren = partsNode.children + newNode
        partsNode.children.clear()
        partsNode.children.addAll(updatedChildren)
        LoadFile(path)
    }

    fun deleteItem(treeNode: TreeNode) {
        deleteFile(treeNode.path)

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