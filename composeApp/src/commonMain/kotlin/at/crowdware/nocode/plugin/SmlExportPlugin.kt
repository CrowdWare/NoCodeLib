package at.crowdware.nocode.plugin


import at.crowdware.nocode.utils.App
import at.crowdware.nocode.utils.Page
import java.io.File


interface SmlExportPlugin {
    val id: String              // z. B. "epub", "bootstrap", "compose"
    val label: String           // z. B. "EPUB 3", "Bootstrap 5"
    val icon: String?           // Optional: "epub.svg", als Pfad oder Ressource

    fun export(app: App, pages: List<Page>, outputDir: File): ExportStatus
}