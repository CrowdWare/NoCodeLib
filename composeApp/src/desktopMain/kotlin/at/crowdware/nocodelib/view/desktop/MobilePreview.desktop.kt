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

package at.crowdware.nocodelib.view.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import at.crowdware.nocodelib.utils.UIElement
import at.crowdware.nocodelib.viewmodel.GlobalProjectState
import org.jcodec.api.FrameGrab
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI


@Composable
actual fun dynamicImageFromAssets(modifier: Modifier, element: UIElement.ImageElement) {
    dynamicImageFromAssets(modifier, element.src, element.scale, element.link, 0, 0)
}

@Composable
actual fun dynamicImageFromAssets(modifier: Modifier, src: String, scale: String, link: String, width: Int, height: Int) {
    val ps = GlobalProjectState.projectState
    val path = "${ps?.folder}/images/${src}"

    val imageFile = File(path)
    var bitmap: ImageBitmap = ImageBitmap(1, 1)
    if (imageFile.exists()) {
        try {
            bitmap = loadImageBitmap(imageFile.inputStream())
        } catch (e: Exception) {
            return
        }
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = when(scale) {
                "crop" -> ContentScale.Crop
                "fit" -> ContentScale.Fit
                "inside" -> ContentScale.Inside
                "fillwidth" -> ContentScale.FillWidth
                "fillbounds" -> ContentScale.FillBounds
                "fillheight" -> ContentScale.FillHeight
                "none" -> ContentScale.None
                else -> ContentScale.Fit
            },
            modifier = if(width == 0)(modifier.fillMaxWidth()) else (modifier.fillMaxWidth(width/100f))
        )
    } else {
        Text(text = "Image not found: ${src}", style = TextStyle(color = MaterialTheme.colors.onPrimary))
    }
}

@Composable
actual fun dynamicSoundfromAssets(filename: String) {
    //Text(text="Sound not found: $filename", style = TextStyle(color = MaterialTheme.colors.onPrimary))
}

@Composable
actual fun dynamicVideofromAssets(modifier: Modifier, filename: String) {
    val ps = GlobalProjectState.projectState
    val path = "${ps?.folder}/videos/$filename"
    var bitmap: BufferedImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB)
    try {
        val picture: Picture = FrameGrab.getFrameFromFile(File(path), 0)
        bitmap = AWTUtil.toBufferedImage(picture)
    } catch (e: Exception) {
        println("${e.message}")
        return
    }
    Image(
        bitmap = bitmap.toComposeImageBitmap(),
        contentDescription = "Video Thumbnail",
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
actual fun dynamicVideofromUrl(modifier: Modifier) {
    Image(
        painter = painterResource("images/video.png"),
        contentDescription = "Description of the image",
        modifier = modifier.fillMaxWidth()
    )
}

actual fun loadPage(pageId: String) {
    val ps = GlobalProjectState.projectState
    // TODO choose right folder e.g. pages-de, pages-en

    val folder = ps?.path?.substringBeforeLast("/")
    val id = pageId.substringAfter("app.")
    println("folder: $folder, page: $id")
    ps?.LoadFile("$folder/$id")
}

actual fun openWebPage(url: String) {
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: Exception) {
        println("Error opening webpage: ${e.message}")
    }
}

@Composable
actual fun dynamicYoutube(modifier: Modifier) {
    Image(
        painter = painterResource("images/youtube.png"),
        contentDescription = "Description of the image",
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
actual fun dynamicScene(modifier: Modifier, width: Int, height: Int) {
    Image(
        painter = painterResource("images/scene.png"),
        contentDescription = "Description of the image",
        modifier = modifier
            .then(if(width > 0) Modifier.width(width.dp) else Modifier)
            .then(if(height > 0) Modifier.height(height.dp) else Modifier)
    )
}