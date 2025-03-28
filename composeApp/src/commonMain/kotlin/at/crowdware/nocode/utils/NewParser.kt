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

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.Parser
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.lexer.*
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.utils.Tuple7
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

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

fun parseApp(sml: String): Pair<App?, String?> {
    ElementRegistry.initDefaults()

    val rootList = SmlGrammar.parseToEnd(sml)
    val root = convertTupleToSmlNode(rootList.firstOrNull() ?: return null to "Empty SML")
    val app = root?.let { buildElementTree(it) } as? App
    return app to if (app == null) "Root must be App" else null
}

fun String.lineWrap(maxLen: Int): String =
    this.chunked(maxLen).joinToString("\n")

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

    if (instance is Page) {
        instance.elements.addAll(node.children.mapNotNull {
            buildElementTree(it) as? UIElement
        })
    }

    if (instance is UIElement) {
        instance.uiElements.addAll(node.children.mapNotNull { buildElementTree(it) as? UIElement })
    }

    return instance
}

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

// Datenklassen-Simulation

data class SmlNode(
    val name: String,
    val properties: Map<String, PropertyValue>,
    val children: List<SmlNode>
)

sealed class PropertyValue {
    data class StringValue(val value: String) : PropertyValue()
    data class IntValue(val value: Int) : PropertyValue()
    data class FloatValue(val value: Float) : PropertyValue()
    data class BoolValue(val value: Boolean) : PropertyValue()
    data class ElementValue(val name: String, val properties: Map<String, PropertyValue>) : PropertyValue()
}

fun parsePadding(padding: String): Padding {
    val paddingValues = padding.split(" ").mapNotNull { it.toIntOrNull() }

    return when (paddingValues.size) {
        1 -> Padding(paddingValues[0], paddingValues[0], paddingValues[0], paddingValues[0]) // Alle Seiten gleich
        2 -> Padding(paddingValues[0], paddingValues[1], paddingValues[0], paddingValues[1]) // Vertikal und Horizontal gleich
        4 -> Padding(paddingValues[0], paddingValues[1], paddingValues[2], paddingValues[3]) // Oben, Rechts, Unten, Links
        else -> Padding(0, 0, 0, 0)
    }
}