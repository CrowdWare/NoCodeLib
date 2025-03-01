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
* 
# SML GRAMMAR for AI requests for SML generation


// Tokens
TOKEN identifier: "[a-zA-Z_][a-zA-Z0-9_]*"
TOKEN lBrace: "{"
TOKEN rBrace: "}"
TOKEN colon: ":"
TOKEN stringLiteral: "\"[^\"]*\""
TOKEN whitespace: "\\s+"
TOKEN integerLiteral: "\\d+"
TOKEN floatLiteral: "\\d+\\.\\d+"
TOKEN lineComment: "//.*"
TOKEN blockComment: "/\\*[\\s\\S]*?\\*/"

// Grammar Rules
Grammar SmlGrammar {

    // Whitespace and comments are ignored
    ignored: whitespace | lineComment | blockComment

    // Property Value Types
    stringValue: stringLiteral -> PropertyValue.StringValue(value)
    intValue: integerLiteral -> PropertyValue.IntValue(value.toInt())
    floatValue: floatLiteral -> PropertyValue.FloatValue(value.toFloat())

    // Define Property Value
    propertyValue: floatValue | intValue | stringValue

    // Define a Property
    property: ignored* identifier ignored* colon ignored* propertyValue -> (id, value)

    // Element Content can contain properties or nested elements
    elementContent: (property | element)*

    // Define an Element
    element: ignored* identifier ignored* lBrace elementContent ignored* rBrace

    // Root Parser for the entire structure
    root: (element+ ignored*) -> elements
}