package at.crowdware.nocode.plugin


import androidx.compose.runtime.Composable
import at.crowdware.nocode.utils.SmlNode
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

interface AppEditorPlugin {
    @Composable
    fun editor(
        source: File,
        node: SmlNode
    )
}