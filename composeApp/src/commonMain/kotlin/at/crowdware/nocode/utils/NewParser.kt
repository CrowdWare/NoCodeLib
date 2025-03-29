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

package at.crowdware.nocode.utils

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.utils.Tuple7
import com.github.h0tk3y.betterParse.grammar.*
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
/*
object ElementRegistry {
    private val typeMap = mutableMapOf<String, KClass<out Any>>()

    fun register(type: String, clazz: KClass<out Any>) {
        typeMap[type] = clazz
    }

    fun get(type: String): KClass<out Any>? = typeMap[type]

    fun initDefaults() {
        register("App", App::class)
        register("Page", Page::class)
        register("Theme", ThemeElement::class)
        register("Deployment", DeploymentElement::class)

        register("Scene", UIElement.SceneElement::class)
        register("Image", UIElement.ImageElement::class)
        register("AsyncImage", UIElement.AsyncImageElement::class)
        register("Spacer", UIElement.SpacerElement::class)
        register("Video", UIElement.VideoElement::class)
        register("Youtube", UIElement.YoutubeElement::class)
        register("Sound", UIElement.SoundElement::class)
        register("Text", UIElement.TextElement::class)
        register("Box", UIElement.BoxElement::class)
        register("Column", UIElement.ColumnElement::class)
        register("LazyColumn", UIElement.LazyColumnElement::class)
        register("LazyRow", UIElement.LazyRowElement::class)
        register("Row", UIElement.RowElement::class)
        register("Button", UIElement.ButtonElement::class)
        register("Markdown", UIElement.MarkdownElement::class)
    }
}
*/
fun convertTupleToSmlNode(tuple: Any): SmlNode? {
    if (tuple !is Tuple7<*, *, *, *, *, *, *>) return null

    val nameToken = tuple.t2
    val content = tuple.t5 as? List<*> ?: return null

    val name = (nameToken as? TokenMatch)?.text ?: return null

    val properties = mutableMapOf<String, PropertyValue>()
    val children = mutableListOf<SmlNode>()

    content.forEach {
        when (it) {
            is Pair<*, *> -> {
                val key = it.first as? String ?: return@forEach
                val value = it.second as? PropertyValue ?: return@forEach
                properties[key] = value
            }
            is Tuple7<*, *, *, *, *, *, *> -> {
                convertTupleToSmlNode(it)?.let { node -> children.add(node) }
            }
        }
    }

    return SmlNode(name, properties, children)
}

/*
fun parseApp(sml: String): Pair<App?, String?> {
    ElementRegistry.initDefaults()

    val rootList = SmlGrammar.parseToEnd(sml)
    val root = convertTupleToSmlNode(rootList.firstOrNull() ?: return null to "Empty SML")
    val app = root?.let { buildElementTree(it) } as? App

    println("Theme: ${app?.theme}")
    return app to if (app == null) "Root must be App" else null
}
*/
fun String.lineWrap(maxLen: Int): String =
    this.chunked(maxLen).joinToString("\n")
/*
fun parsePage(sml: String, lang: String): Pair<Page?, String?> {
    ElementRegistry.initDefaults()

    val rootList = try {
        SmlGrammar.parseToEnd(sml)
    } catch (e: Exception) {
        return null to "Parsefehler: ${e.message?.lineWrap(100)}"
    }

    val root = convertTupleToSmlNode(rootList.firstOrNull() ?: return null to "Leeres SML")
    val page = root?.let { buildElementTree(it) } as? Page
    page?.language = lang // trigger recompose

    return page to if (page == null) "Root muss Page sein" else null
}
*/
/*
fun buildElementTree(node: SmlNode): Any? {
    val clazz = ElementRegistry.get(node.name) ?: return null
    val constructor = clazz.primaryConstructor ?: return null

    val args = constructor.parameters.mapNotNull { param ->
        if (param.name == "elements") return@mapNotNull null

        val prop = node.properties[param.name]
        val value = when (prop) {
            is PropertyValue.StringValue -> {
                if (param.name == "padding") parsePadding(prop.value)
                else prop.value
            }
            is PropertyValue.FloatValue -> prop.value
            is PropertyValue.BoolValue -> prop.value
            is PropertyValue.ElementValue -> buildElementTree(
                SmlNode(prop.name, prop.properties, emptyList())
            )
            else -> null
        }

        //println("➡️ Parameter: ${param.name}, expected: ${param.type}, actual: ${value?.let { it::class.simpleName }}, value=$value")

        if (value != null) param to value else null
    }.toMap()


    val instance = try {
        constructor.callBy(args)
    } catch (e: InvocationTargetException) {
        println("❌ Fehler beim Erzeugen von ${clazz.simpleName}: ${e.targetException}")
        e.targetException.printStackTrace() // Die echte Exception im Konstruktor
        return null
    } catch (e: Exception) {
        println("❌ Allgemeiner Fehler bei ${clazz.simpleName}: ${e.message}")
        e.printStackTrace()
        return null
    }

    if (instance is ThemeElement) {

        // Handle ThemeElement properties generically
        val themeProperties = node.properties
        ThemeElement::class.members.forEach { member ->
            if (member is kotlin.reflect.KMutableProperty<*>) {
                val propertyName = member.name
                themeProperties[propertyName]?.let { propValue ->
                    when (propValue) {
                        is PropertyValue.StringValue -> member.setter.call(instance, propValue.value)
                        is PropertyValue.BoolValue -> member.setter.call(instance, propValue.value)
                        is PropertyValue.FloatValue -> member.setter.call(instance, propValue.value)
                        is PropertyValue.IntValue -> member.setter.call(instance, propValue.value)
                        is PropertyValue.ElementValue -> { }
                        else -> {

                        }
                    }
                }
            }
        }
    } else if (instance is Page) {
        instance.elements.addAll(node.children.mapNotNull {
            buildElementTree(it) as? UIElement
        })
    }

    if (instance is UIElement) {
        instance.uiElements.addAll(node.children.mapNotNull { buildElementTree(it) as? UIElement })
    }

    return instance
}
*/

val identifier: Token = regexToken("[a-zA-Z_][a-zA-Z0-9_]*")
val lBrace: Token = literalToken("{")
val rBrace: Token = literalToken("}")
val colon: Token = literalToken(":")
val stringLiteral: Token = regexToken("\"[^\"]*\"")
val whitespace: Token = regexToken("\\s+")
val integerLiteral: Token = regexToken("\\d+")
val floatLiteral = regexToken("\\d+\\.\\d+")

val lineComment: Token = regexToken("//.*")
val blockComment: Token = regexToken(Regex("/\\*[\\s\\S]*?\\*/", RegexOption.DOT_MATCHES_ALL))

object SmlGrammar : Grammar<List<Any>>() {
    val whitespaceParser = zeroOrMore(whitespace)

    val commentParser = lineComment or blockComment

    val ignoredParser = zeroOrMore(whitespace or commentParser)

    val stringParser = stringLiteral.map { PropertyValue.StringValue(it.text.removeSurrounding("\"")) }
    val integerParser = integerLiteral.map { PropertyValue.IntValue(it.text.toInt()) }
    val floatParser = floatLiteral.map { PropertyValue.FloatValue(it.text.toFloat()) }

    val propertyValue = floatParser or integerParser or stringParser

    val property by (ignoredParser and identifier and ignoredParser and colon and ignoredParser and propertyValue).map { (_, id, _, _, _, value) ->
        id.text to value
    }
    val elementContent: Parser<List<Any>> = zeroOrMore(property or parser { element })
    val element: Parser<Any> by ignoredParser and identifier and ignoredParser and lBrace and elementContent and ignoredParser and rBrace

    override val tokens: List<Token> = listOf(
        identifier, lBrace, rBrace, colon, stringLiteral, floatLiteral, integerLiteral,
        whitespace, lineComment, blockComment
    )
    override val rootParser: Parser<List<Any>> = (oneOrMore(element) and ignoredParser).map { (elements, _) -> elements }
}

data class SmlNode(
    val name: String,
    val properties: Map<String, PropertyValue>,
    val children: List<SmlNode>
)

sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
    //data class BoolValue(val value: Boolean) : PropertyValue()
    //data class ElementValue(val name: String, val properties: Map<String, PropertyValue>) : PropertyValue()
}

val fontWeightMap = mapOf(
    "bold" to FontWeight.Bold,
    "black" to FontWeight.Black,
    "thin" to FontWeight.Thin,
    "extrabold" to FontWeight.ExtraBold,
    "extralight" to FontWeight.ExtraLight,
    "light" to FontWeight.Light,
    "medium" to FontWeight.Medium,
    "semibold" to FontWeight.SemiBold,
    "" to FontWeight.Normal
)

val textAlignMap = mapOf(
    "left" to TextAlign.Start,
    "center" to TextAlign.Center,
    "right" to TextAlign.End,
    "" to TextAlign.Unspecified
)

fun getStringValue(node: SmlNode, key: String, default: String): String {
    val value = node.properties[key]
    return when {
        value is PropertyValue.StringValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not a StringValue (found: $type). Returning default value: \"$default\"")
            default
        }
        else -> default
    }
}

fun getIntValue(node: SmlNode, key: String, default: Int): Int {
    val value = node.properties[key]
    return when {
        value is PropertyValue.IntValue -> value.value
        value is PropertyValue -> {
            val type = value.javaClass.simpleName
            println("Warning: The value for '$key' is not an IntValue (found: $type). Returning default value: $default")
            default
        }
        else -> default
    }
}

fun getFontWeight(node: SmlNode): FontWeight {
    val key = getStringValue(node, "fontWeight", "").trim()
    return fontWeightMap.getOrDefault(key, FontWeight.Normal)
}
fun getTextAlign(node: SmlNode): TextAlign {
    val key = getStringValue(node, "textAlign", "").trim()
    return textAlignMap.getOrDefault(key, TextAlign.Unspecified)
}

fun getPadding(node: SmlNode): Padding {
    val paddingString = getStringValue(node, "padding", "0")
    val paddingValues = paddingString.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // All sides the same
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertical and Horizontal same
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Top, Right, Bottom, Left
        else -> Padding(0, 0, 0, 0) // Default fallback
    }
}

fun parseSML(sml: String): Pair<SmlNode?, String?> {
    val rootList = try {
        SmlGrammar.parseToEnd(sml)
    } catch (e: Exception) {
        return null to "ParseError: ${e.message?.lineWrap(100)}"
    }
    return rootList.firstOrNull()?.let { convertTupleToSmlNode(it) } to null
}


fun fillAppFromSmlNode(appNode: SmlNode): App {
    val app = App()

    appNode.properties.forEach { (key, value) ->
        when (key) {
            "name" -> app.name = (value as? PropertyValue.StringValue)?.value ?: ""
            "description" -> app.description = (value as? PropertyValue.StringValue)?.value ?: ""
            "icon" -> app.icon = (value as? PropertyValue.StringValue)?.value ?: ""
            "id" -> app.id = (value as? PropertyValue.StringValue)?.value ?: ""
            "smlVersion" -> app.smlVersion = (value as? PropertyValue.StringValue)?.value ?: "1.1"
            "author" -> app.author = (value as? PropertyValue.StringValue)?.value ?: ""
        }
    }

    val themeNode = appNode.children.find { it.name == "Theme" }
    app.theme = themeNode?.let { fillThemeFromSmlNode(it) } ?: ThemeElement()
    return app
}

fun fillThemeFromSmlNode(themeNode: SmlNode): ThemeElement {
    val theme = ThemeElement()

    // Durch die Properties des Theme-Nodes iterieren
    themeNode.properties.forEach { (key, value) ->
        when (key) {
            "primary" -> theme.primary = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onPrimary" -> theme.onPrimary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "background" -> theme.background = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onBackground" -> theme.onBackground = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "secondary" -> theme.secondary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onSecondary" -> theme.onSecondary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "secondaryContainer" -> theme.secondaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onSecondaryContainer" -> theme.onSecondaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "tertiary" -> theme.tertiary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onTertiary" -> theme.onTertiary = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "tertiaryContainer" -> theme.tertiaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "onTertiaryContainer" -> theme.onTertiaryContainer = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "error" -> theme.error = (value as? PropertyValue.StringValue)?.value ?: "#FF0000"
            "onError" -> theme.onError = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "errorContainer" -> theme.errorContainer = (value as? PropertyValue.StringValue)?.value ?: "#FF0000"
            "onErrorContainer" -> theme.onErrorContainer = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "surface" -> theme.surface = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onSurface" -> theme.onSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "surfaceVariant" -> theme.surfaceVariant = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "onSurfaceVariant" -> theme.onSurfaceVariant = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "outline" -> theme.outline = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "outlineVariant" -> theme.outlineVariant = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "inversePrimary" -> theme.inversePrimary = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "inverseSurface" -> theme.inverseSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "inverseOnSurface" -> theme.inverseOnSurface = (value as? PropertyValue.StringValue)?.value ?: "#000000"
            "surfaceTint" -> theme.surfaceTint = (value as? PropertyValue.StringValue)?.value ?: "#FFFFFF"
            "scrim" -> theme.scrim = (value as? PropertyValue.StringValue)?.value ?: "#000000"
        }
    }

    return theme
}