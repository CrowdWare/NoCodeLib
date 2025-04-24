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

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import at.crowdware.nocode.utils.SmlNode
//import at.crowdware.nocode.utils.UIElement
import at.crowdware.nocode.utils.getIntValue
import at.crowdware.nocode.utils.getPadding
import at.crowdware.nocode.utils.getStringValue
import at.crowdware.nocode.viewmodel.GlobalProjectState
import coil3.compose.AsyncImage
import org.jcodec.api.FrameGrab
import org.jcodec.common.model.Picture
import org.jcodec.scale.AWTUtil
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.net.URI


@Composable
actual fun dynamicImageFromAssets(modifier: Modifier, node: SmlNode, dataItem: Any) {
    val src = getStringValue(node, "src", "")
    val scale = getStringValue(node, "scale", "")
    val link = getStringValue(node, "link", "")
    val width = getIntValue(node, "width", 0)
    val height = getIntValue(node, "height", 0)
    dynamicImageFromAssets(modifier, src, scale, link, width, height, dataItem)
}

@Composable
actual fun dynamicImageFromAssets(modifier: Modifier, src: String, scale: String, link: String, width: Int, height: Int, dataItem: Any) {
    var fileName = src
    var isExternal  = false
    var _link = link
    if (src.startsWith("<") && src.endsWith(">")) {
        val fieldName = src.substring(1, src.length - 1)
        if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
            val url = dataItem[fieldName] as? String
            fileName = "$url"
            isExternal = true
        }
    }
    if(link.startsWith("<") && link.endsWith(">")) {
        val fieldName = link.substring(1, link.length - 1)
        if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
            val value = dataItem[fieldName] as? String
            _link = "$value"
        }
    }
    val ps = GlobalProjectState.projectState
    val path = "${ps?.folder}/images/${fileName}"

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
            modifier = modifier
                .then(if (width == 0) Modifier.fillMaxWidth() else Modifier.fillMaxWidth(width / 100f))
                .then(if (height == 0) Modifier.wrapContentHeight() else Modifier.fillMaxHeight(height / 100f))
        )
    } else {
        Text(text = "Image not found: ${fileName}", style = TextStyle(color = MaterialTheme.colors.onPrimary))
    }
}

@Composable
actual fun asyncImage(
    modifier: Modifier,
    node: SmlNode,
    dataItem: Any
) {
    val width = getIntValue(node, "width", 0)
    val height = getIntValue(node, "height", 0)
    val scale = getStringValue(node, "scale", "")
    var src = getStringValue( node,"src", "")
    var link = getStringValue(node, "link", "")
    val padding = getPadding(node)
    if (src.startsWith("<") && src.endsWith(">")) {
        val fieldName = src.substring(1, src.length - 1)
        if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
            val url = dataItem[fieldName] as? String
            src = "$url"
        }
    }
    if (link.startsWith("<") && link.endsWith(">")) {
        val fieldName = link.substring(1, link.length - 1)
        if (dataItem is Map<*, *> && fieldName.isNotEmpty()) {
            val value = dataItem[fieldName] as? String
            link = "$value"
        }
    }

    println("asyncImage: $src")
    AsyncImage(
        modifier = modifier.padding(padding.left.dp, padding.top.dp, padding.right.dp,padding.bottom.dp),
        model = src,
        contentScale = when(scale.lowercase()) {
            "crop" -> ContentScale.Crop
            "fit" -> ContentScale.Fit
            "inside" -> ContentScale.Inside
            "fillwidth" -> ContentScale.FillWidth
            "fillbounds" -> ContentScale.FillBounds
            "fillheight" -> ContentScale.FillHeight
            "none" -> ContentScale.None
            else -> ContentScale.Fit
        },
        contentDescription = null
    )
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