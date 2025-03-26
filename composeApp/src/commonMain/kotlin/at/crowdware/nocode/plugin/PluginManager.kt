package at.crowdware.nocode.plugin


object PluginManager {
    private val plugins = mutableListOf<SmlExportPlugin>()

    fun register(plugin: SmlExportPlugin) {
        plugins.add(plugin)
    }

    fun all(): List<SmlExportPlugin> = plugins.toList()

    fun getById(id: String): SmlExportPlugin? =
        plugins.find { it.id == id }
}