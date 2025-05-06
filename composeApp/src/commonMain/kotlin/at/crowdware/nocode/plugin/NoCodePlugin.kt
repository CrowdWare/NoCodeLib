package at.crowdware.nocode.plugin


import java.io.File


interface NoCodePlugin {
    val id: String              // z. B. "epub", "bootstrap", "compose"
    val label: String           // z. B. "EPUB 3", "Bootstrap 5"
    val icon: String?           // Optional: "epub.svg", als Pfad oder Ressource
}

interface ExportPlugin {
    suspend fun export(
        source: String,
        outputDir: File,
        onLog: (String) -> Unit = {}
    ): ExportStatus
}

interface EditorPlugin {
    suspend fun editor(
        source: File,
        onLog: (String) -> Unit = {}
    )
}