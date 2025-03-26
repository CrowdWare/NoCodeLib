package at.crowdware.nocode.plugin

import java.io.File

data class ExportStatus(
    val success: Boolean,
    val message: String,
    val files: List<File> = emptyList()
)