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

package at.crowdware.nocode.view.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import at.crowdware.nocode.plugin.ExportPlugin
import at.crowdware.nocode.plugin.PluginManager
import at.crowdware.nocode.theme.AppTheme
import at.crowdware.nocode.ui.HoverableIcon
import at.crowdware.nocode.utils.*
import at.crowdware.nocode.viewmodel.ProjectState
import java.awt.Desktop
import java.io.File
import java.net.URI

@Composable
fun toolbar(currentProject: ProjectState?) {
    val app = currentProject?.app
    Column(
        modifier = Modifier.width(65.dp).fillMaxHeight().background(color = MaterialTheme.colors.primary),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicText(
            text = "Build",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        HoverableIcon(
            onClick = { currentProject?.isNewProjectDialogVisible = true },
            painter = painterResource("drawable/create_folder.xml"),
            tooltipText = "Create new Project",
            isSelected = currentProject?.isNewProjectDialogVisible == true
        )
        HoverableIcon(
            onClick = { currentProject?.isOpenProjectDialogVisible = true },
            painter = painterResource("drawable/open_file.xml"),
            tooltipText = "Open Project",
            isSelected = currentProject?.isOpenProjectDialogVisible == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        HoverableIcon(
            onClick = { currentProject?.isProjectStructureVisible = true },
            painter = painterResource("drawable/tree.xml"),
            tooltipText = "Project Structure",
            isSelected = currentProject?.isProjectStructureVisible == true
        )
        Spacer(modifier = Modifier.height(8.dp))

        if(currentProject?.extension == "sml" && currentProject.fileName != "app.sml") {
            currentProject.isProjectStructureVisible.let {
                HoverableIcon(
                    onClick = { currentProject.isProjectStructureVisible = false },
                    painter = painterResource("drawable/library.xml"),
                    tooltipText = "Widget Palette",
                    isSelected = !it
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        BasicText(
            text = "Plugins",
            modifier = Modifier.padding(8.dp),
            maxLines = 1,
            style = TextStyle(color = MaterialTheme.colors.onPrimary),
            overflow = TextOverflow.Ellipsis
        )

        // here we dynamically list all plugins installed, and execute them on click
        PluginManager.all().forEach { plugin ->
            if (plugin is ExportPlugin) {
                val iconPath =
                    System.getProperty("user.home") + "/Library/Application Support/NoCodeDesigner/plugin-cache/${plugin.id}/${plugin.icon}"
                //val iconPath = ".plugin-cache/${plugin.id}/${plugin.icon}"
                val iconPainter = if (plugin.icon != null && File(iconPath).exists()) {
                    try {
                        loadSvgResource(iconPath)
                    } catch (e: Exception) {
                        painterResource("drawable/course.xml")
                    }
                } else {
                    painterResource("drawable/course.xml")
                }
                Spacer(modifier = Modifier.height(8.dp))
                HoverableIcon(
                    onClick = {
                        currentProject?.exportPlugin = plugin
                        currentProject?.isExportDialogVisible = true
                    },
                    painter = iconPainter!!,
                    tooltipText = plugin.label,
                    isSelected = false
                )
            }
        }
    }
}