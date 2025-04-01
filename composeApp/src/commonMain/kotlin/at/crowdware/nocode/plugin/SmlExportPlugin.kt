package at.crowdware.nocode.plugin


import java.io.File


interface SmlExportPlugin {
    val id: String              // z. B. "epub", "bootstrap", "compose"
    val label: String           // z. B. "EPUB 3", "Bootstrap 5"
    val icon: String?           // Optional: "epub.svg", als Pfad oder Ressource

    fun export(source: String, outputDir: File): ExportStatus
}