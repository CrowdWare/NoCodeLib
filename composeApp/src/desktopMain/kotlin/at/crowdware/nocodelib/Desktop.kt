/*
 * Copyright (C) 2025 CrowdWare
 *
 * This file is part of NoCodeLib.
 *
 *  NoCodeLib is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  NoCodeLib is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with NoCodeLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.crowdware.nocodelib

import androidx.compose.ui.awt.ComposeWindow
import at.crowdware.nocodelib.viewmodel.GlobalAppState
import at.crowdware.nocodelib.viewmodel.GlobalAppState.appState
import at.crowdware.nocodelib.viewmodel.State
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.io.PrintStream

fun onAppClose(frame: ComposeWindow, folder:String, path: String) {
    saveState(frame, folder, path)
}

fun saveState(frame: ComposeWindow, folder: String, path: String) {
    // Save the app state when the window is closed
    saveAppState(
        State(
            windowHeight = frame.height,
            windowWidth = frame.width,
            windowX = frame.x,
            windowY = frame.y,
            lastProject = folder,
            theme = "Dark",
            license = appState?.license.toString()
        ), path
    )
}

fun setupLogging(path: String) {
    val userHome = System.getProperty("user.home")
    val configDirectory = if (System.getProperty("os.name").contains("Windows")) {
        File("$userHome/AppData/Local/$path")
    } else {
        File("$userHome/Library/Application Support/$path")
    }
    val tempFile = File(configDirectory, "$path.log")

    if (!configDirectory.exists()) {
        configDirectory.mkdirs()
    }
    if (!tempFile.exists()) {
        tempFile.createNewFile()
    }

    // Redirect stdout and stderr to the file
    val logStream = PrintStream(tempFile.outputStream())
    System.setOut(logStream)
    System.setErr(logStream)

    println("Logging initialized. Writing to: ${tempFile.absolutePath}")
}

fun saveAppState(state: State, path: String) {
    val userHome = System.getProperty("user.home")
    val configDirectory = if (System.getProperty("os.name").contains("Windows")) {
        File("$userHome/AppData/Local/$path")
    } else {
        File("$userHome/Library/Application Support/$path")
    }

    // Create the directory if it doesn't exist
    if (!configDirectory.exists()) {
        configDirectory.mkdirs()
    }

    val configFile = File(configDirectory, "app_state.json")
    try {
        val jsonState = Json.encodeToString(state)
        configFile.writeText(jsonState)
    } catch (e: IOException) {
        println("Error writing app state: ${e.message}")
        e.printStackTrace()
    }
}

fun loadAppState(path: String) {
    val appState = GlobalAppState.appState
    val userHome = System.getProperty("user.home")
    val configDirectory = if (System.getProperty("os.name").contains("Windows")) {
        File("$userHome/AppData/Local/$path")
    } else {
        File("$userHome/Library/Application Support/$path")
    }
    val configFile = File(configDirectory, "app_state.json")

    if(!configDirectory.exists()) {
        configDirectory.mkdirs()
    }
    try {
        val jsonState = configFile.readText()
        val state = Json.decodeFromString<State>(jsonState)
        if (appState != null) {
            appState.theme = state.theme
            appState.license = state.license
            appState.lastProject = state.lastProject
            appState.windowX = state.windowX
            appState.windowY = state.windowY
            appState.windowWidth = state.windowWidth
            appState.windowHeight = state.windowHeight
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}