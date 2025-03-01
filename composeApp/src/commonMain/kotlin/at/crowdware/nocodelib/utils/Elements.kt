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

package at.crowdware.nocodelib.utils

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import java.time.LocalDateTime

data class App(
    @StringAnnotation("Name of the book.")
    var name: String = "",
    @StringAnnotation("Put a description about the book here.")
    var description: String = "",
    @StringAnnotation("The folder name where you want to deploy the HTML output.")
    var deployDirHtml: String ="",
    @StringAnnotation("Icon for the book. Sample: icon.png")
    var icon: String = "",
    @StringAnnotation("Unique Id of the app. Sample: com.example.bookname")
    var id: String = "",
    @StringAnnotation("Version of the current SML. default is 1.1")
    var smlVersion: String = "1.1",
    @StringAnnotation("The name of the author.")
    var author: String = "",
    @StringAnnotation("A short bio about the author.")
    var authorBio: String = "",
    var course: UIElement.Course = UIElement.Course(),
    var theme: ThemeElement = ThemeElement(),
    var deployment: DeploymentElement = DeploymentElement()
)

data class Ebook (
    @StringAnnotation("Version of the current SML. Default is 1.1")
    var smlVersion: String = "1.1",
    @StringAnnotation("Theme for the book. Atm there is only \"Epub3\".")
    var theme: String = "Epub3",
    @StringAnnotation("Name of the book")
    var name: String = "",
    @StringAnnotation("Language of the book. Atm only \"en\" and \"de\"")
    var language: String = "en",
    @StringAnnotation("The folder name where you want to deploy the EPUB output.")
    var deployDirEpub: String ="",
    @StringAnnotation("Name of the author")
    var creator: String = "",
    @StringAnnotation("Link to the website of the author")
    var creatorLink: String = "#",
    @StringAnnotation("Name of the license. Example: 'All rights reserved'")
    var license: String = "",
    @StringAnnotation("Link to the website of the license")
    var licenseLink: String = "#",
    @StringAnnotation("Link to the book.")
    var bookLink: String = "#",
    val parts: MutableList<PartElement> = mutableListOf()
)

data class ThemeElement(
    var primary: String = "",
    var onPrimary: String = "",
    var primaryContainer: String = "",
    var onPrimaryContainer: String = "",
    var secondary: String = "",
    var onSecondary: String = "",
    var secondaryContainer: String = "",
    var onSecondaryContainer: String = "",
    var tertiary: String = "",
    var onTertiary: String = "",
    var tertiaryContainer: String = "",
    var onTertiaryContainer: String = "",
    var error: String = "",
    var errorContainer: String = "",
    var onError: String = "",
    var onErrorContainer: String = "",
    var background: String = "",
    var onBackground: String = "",
    var surface: String = "",
    var onSurface: String = "",
    var surfaceVariant: String = "",
    var onSurfaceVariant: String = "",
    var outline: String = "",
    var inverseOnSurface: String = "",
    var inverseSurface: String = "",
    var inversePrimary: String = "",
    var surfaceTint: String = "",
    var outlineVariant: String = "",
    var scrim: String = ""
)

data class DeploymentElement(
    val files: MutableList<FileElement> = mutableListOf()
)

data class FileElement(val path: String, val time: LocalDateTime)

data class PartElement (val src: String, val pdfOnly: Boolean = false)

data class Markdown (
    @StringAnnotation("Headers in Markdown allow you to define section titles and headings." +
            "They are created using the `#` symbol, with the number of `#` symbols corresponding to the header level. " +
            "For example:\n" +
            "# Header 1\n"+
            "## Header 2\n" +
            "### Header 3\n")
    var Headers: String,

    @StringAnnotation("Markdown provides various options for styling text, such as bold, italic, and strikethrough. " +
            "These styles help to emphasize important parts of the text.\n" +
            "- **Bold**: Use double asterisks `**text**` or double underscores `__text__` to make text bold.\n" +
            "```\n" +
            "**This is bold text**\n" +
            "```\n" +
            "- **Italic**: Use a single asterisk `*text*` or a single underscore `_text_` for italics.\n" +
            "```\n" +
            "*This is italic text*\n" +
            "```\n" +
            "- **Strikethrough**: Use double tildes `~~text~~` for strikethrough text.\n" +
            "```\n" +
            "~~This text is crossed out~~\n" +
            "```\n")
    var Styling: String,

    @StringAnnotation("Markdown also allows the creation of hyperlinks, which link to external or internal content. \n" +
            "Use square brackets `[]` for the link text and parentheses `()` for the URL.\n" +
            "**Syntax:**\n" +
            "`[Link Text](https://example.com)`\n" +
            "**Example:**\n" +
            "For more information, visit the [Markdown Guide](https://www.markdownguide.org).\n")
    var Hyperlinks: String,

    @StringAnnotation("Images can be embedded in Markdown similarly to hyperlinks, with an exclamation mark ! at the beginning, " +
            "followed by the alt text in square brackets `[]` and the image URL in parentheses `().\n" +
            "**Syntax:**\n" +
            "`![Alt Text](https://example.com/image.jpg)`\n")
    var Images: String,

    @StringAnnotation("We have implemented some of the possible special characters.\n" +
            "`(c)` renders a © copyright symbol\n" +
            "`(r)` renders a ® registered brand symbol\n" +
            "`(tm)` renders a ™ trademark symbol ")
    var Special: String
)

@ElementAnnotation("A **Page** is the base element of the NoCodeApp. You can put all other Elements inside a Page.")
data class Page(
    @StringAnnotation("Give the page a title which will be the headline in the book reader.")
    var title: String,

    @HexColorAnnotation
    var color: String,

    @HexColorAnnotation
    var backgroundColor: String,

    @PaddingAnnotation
    var padding: Padding,

    @StringAnnotation("You can enter boolean values like **\"true\"** and **\"false\"**. Sample **scrollable: \"true\"**")
    var scrollable: String,

    @IgnoreForDocumentation
    val elements: MutableList<UIElement>)

sealed class UIElement {
    data object Zero : UIElement()

    data class Course(
        var topics: MutableList<Topic> = mutableListOf()
    ) : UIElement()

    data class Topic(
        val label: String,
        val page: String? = null,
        val subtopics: MutableList<Subtopic> = mutableListOf()
    ) : UIElement()

    data class Subtopic(
        val label: String,
        val id: String? = null,
    ) : UIElement()

    @ElementAnnotation("With a **Text** element you can render text on the page.")
    data class TextElement(
        @StringAnnotation
        val text: String,

        @HexColorAnnotation
        val color: String,

        @IntAnnotation
        val fontSize: TextUnit,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        @StringAnnotation("Enter one of the following weights normal, extralight, thin, medium, bold, semibold, extrabold, black like **fontWeight: \"bold\"**")
        val fontWeight: FontWeight,

        @StringAnnotation("Enter one of the following alignments left, center, right like **textAlign: \"center\"**")
        val textAlign: TextAlign
    ) : UIElement()

    @ElementAnnotation("With a **Button** element you can render a clickable button on the page. With a click you can load other pages or external websites.")
    data class ButtonElement(
        @StringAnnotation
        val label: String,

        @HexColorAnnotation
        val backgroundColor: String,

        @HexColorAnnotation
        val color: String,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        @LinkAnnotation
        val link: String) : UIElement()

    @ElementAnnotation("With an **Image** element you can display an image on the page.")
    data class ImageElement(
        @StringAnnotation("Enter the name of the image file like **src: \"sample.png\"**.\nThe image file should be imported into assets first.")
        val src: String,

        @StringAnnotation("Enter the value for scale like crop, fit, inside, fillbounds, fillheight, fillwidth, none.\nSample: **scale: \"fit\"**")
        val scale: String,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        @LinkAnnotation
        val link: String) : UIElement()

    @ElementAnnotation("With a **Spacer** element you can create a visual distance between other elements on the page.")
    data class SpacerElement(
        @IntAnnotation
        val amount: Int,

        @WeightAnnotation
        val weight: Int

        ) : UIElement()

    @ElementAnnotation("With a **Video** element you can show and play videos on the page.")
    data class VideoElement(
        @StringAnnotation("Enter the name of the video file like **src: \"sample.mp4\"**.\nThe video file should be imported into assets first.\nYou can also specify a URL to stream a video from an online source, like **src: \"http://example.com/sample.mp4\"**")
        val src: String,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        ) : UIElement()

    @ElementAnnotation("With a **Youtube** element you can show and play YouTube videos on the page.")
    data class YoutubeElement(
        @StringAnnotation("Enter the YouTube video id in quotes like **id:\"FCyiuG\"**")
        val id: String,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int
        ) : UIElement()

    @ElementAnnotation("With a **Sound** element you can play sounds when the page is loaded.")
    data class SoundElement(
        @StringAnnotation("Enter the name of the sound file like **src: \"sample.mp3\"**.\nThe sound file should be imported into assets first.")
        val src: String) : UIElement()

    @ElementAnnotation("With a **Row** element you can arrange elements horizontally on the page.")
    data class RowElement(
        @PaddingAnnotation
        val padding: Padding,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()
    @ElementAnnotation("With a **Column** element you can arrange elements vertically on the page.")
    data class ColumnElement(
        @PaddingAnnotation
        val padding: Padding,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        val uiElements: MutableList<UIElement> = mutableListOf()) : UIElement()

    @ElementAnnotation("With a **Markdown** element you can render styled text on the page.")
    data class MarkdownElement(
        @MarkdownAnnotation
        val text: String,

        @StringAnnotation("Name of the part (from the ebook project) which will be inserted here. Like: home.md")
        val part: String,

        @HexColorAnnotation
        val color: String,

        @IntAnnotation
        val fontSize: TextUnit,

        @StringAnnotation("Enter one of the following weights normal, extralight, thin, medium, bold, semibold, extrabold, black like **fontWeight: \"bold\"**")
        val fontWeight: FontWeight,

        @StringAnnotation("Enter one of the following alignments left, center, right like **textAlign: \"center\"**")
        val textAlign: TextAlign,

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

    ) : UIElement()

    @ElementAnnotation("With a **Scene** element you can render in 3D scenes. These scenes can also be interactive tutorials, movies whatever you can imagine and build with 3D models.")
    data class SceneElement(

        @WeightAnnotation
        val weight: Int,

        @IntAnnotation
        val width: Int,

        @IntAnnotation
        val height: Int,

        @StringAnnotation("The name of the glb model object file to be rendered. Sample: **model: puppet.glb**")
        val glb: String,

        @StringAnnotation("The name of the gltf model object file to be rendered. Sample: **model: puppet.gltf**")
        val gltf: String,

        @StringAnnotation("The name of the indirect light source texture file to be rendered. KTX files can be rendered. Sample: **environment: light.ktx**")
        val ibl: String,

        @StringAnnotation("The name of the skybox texture file to be rendered. KTX files can be rendered. Sample: **environment: forest.ktx**")
        val skybox: String
    ) : UIElement()
}

data class Padding(val top: Int, val right: Int, val bottom: Int, val left: Int)


