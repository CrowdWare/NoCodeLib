package at.crowdware.nocode.plugin

import kotlinx.serialization.Serializable

@Serializable
data class PluginMetadata(
    val id: String,                 // z. B. "bootstrap5"
    val label: String,              // Anzeigename für UI
    val version: String,            // z. B. "1.0.0"
    val entry: String,              // Pfad zur .jar im Plugin-ZIP
    val mainClass: String,          // Vollqualifizierte Klassenbezeichnung
    val icon: String? = null        // optional: z. B. "icon.svg"
)