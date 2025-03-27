package at.crowdware.nocode.utils

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.loadSvgPainter
import androidx.compose.ui.unit.Density
import java.io.File


fun loadSvgResource(path: String): Painter? {
    return try {
        val file = File(path)
        if (!file.exists()) return null

        file.inputStream().use {
            loadSvgPainter(it, Density(1f))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}