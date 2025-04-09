package at.crowdware.nocode.plugin

import kotlinx.serialization.json.Json
import java.io.File
import java.net.URLClassLoader
import java.util.zip.ZipFile


object PluginManager {
    private val plugins = mutableListOf<SmlExportPlugin>()

    fun register(plugin: SmlExportPlugin) {
        plugins.add(plugin)
    }

    fun all(): List<SmlExportPlugin> = plugins.toList()

    fun getById(id: String): SmlExportPlugin? =
        plugins.find { it.id == id }

    fun loadAllFromPluginsFolder(folder: File): List<SmlExportPlugin> {
        val plugins = mutableListOf<SmlExportPlugin>()
        if (!folder.exists() || !folder.isDirectory) return plugins

        // create an empty folder for the plugins
        val pluginDir = File(System.getProperty("user.home") + "/Library/Application Support/NoCodeDesigner/plugin-cache/")
        if (pluginDir.exists())
            pluginDir.deleteRecursively()
        else
            pluginDir.mkdirs()

        val pluginZips = folder.listFiles { file ->
            file.extension == "zip" && file.name.endsWith(".zip")
        } ?: return plugins

        for (zip in pluginZips) {
            val pluginId = zip.name.substringBefore(".")
            loadPluginFromZip(zip, File(pluginDir, pluginId))?.let { plugin ->
                plugins.add(plugin)
                register(plugin)
            }
        }
        return plugins
    }

    fun loadPluginFromZip(zipFile: File, pluginDir: File): SmlExportPlugin? {
        try {
            // Entpacken
            ZipFile(zipFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val outFile = File(pluginDir, entry.name)
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            outFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }

            // plugin.json lesen
            val pluginJsonFile = File(pluginDir, "plugin.json")
            if (!pluginJsonFile.exists()) {
                println("⚠️ plugin.json fehlt in ${zipFile.name}")
                return null
            }

            val metadata = Json.decodeFromString<PluginMetadata>(pluginJsonFile.readText())
            val jarFile = File(pluginDir, metadata.entry)
            if (!jarFile.exists()) {
                println("⚠️ JAR nicht gefunden: ${metadata.entry}")
                return null
            }

            // Plugin-Klasse laden
            val loader = URLClassLoader(arrayOf(jarFile.toURI().toURL()), PluginManager::class.java.classLoader)
            val clazz = Class.forName(metadata.mainClass, true, loader)
            val instance = clazz.getDeclaredConstructor().newInstance()

            if (instance is SmlExportPlugin) {
                println("✅ Plugin geladen: ${metadata.label} (${metadata.id})")
                return instance
            } else {
                println("❌ ${metadata.mainClass} ist kein SmlExportPlugin")
            }

        } catch (e: Exception) {
            println("❌ Fehler beim Laden von ${zipFile.name}: ${e.message}")
            e.printStackTrace()
        }

        return null
    }
}

