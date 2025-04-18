package at.crowdware.nocode.utils

import java.io.File
import java.util.Properties

class DesktopPreferences(private val file: File) {
    private val props = Properties().apply {
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String> {
        return props.getProperty(key)?.split(";")?.toSet() ?: default
    }

    fun putStringSet(key: String, value: Set<String>) {
        props.setProperty(key, value.joinToString(";"))
        file.outputStream().use { props.store(it, null) }
    }
}