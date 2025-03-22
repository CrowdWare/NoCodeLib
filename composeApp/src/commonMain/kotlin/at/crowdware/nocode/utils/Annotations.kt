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


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ElementAnnotation(val description: String = """Enter text using Markdown syntax to format the content.
Supported elements include:

- **Headings**: Use `#` for headings `##` for sub headings up to `######`.
- **Bold Text**: Wrap text in double asterisks (e.g., `**bold text**`).
- **Italic Text**: Wrap text in single asterisks (e.g., `*italic text*`).
- **Bold Italic Text**: Wrap text in triple asterisks (e.g., `***bold italic text***`).
- **Strikethrough**: Wrap text in double tildes (e.g., `~~strikethrough~~`).
- Also you can simulate special characters like **(c)** for `(c)` and **(tm)** for `(tm)` and **(r)** for `(r)`.
""")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreForDocumentation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BoolAnnotation(val description: String = "You can enter boolean value like **true** and **false**. Sample **scrollable: true**")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MarkdownAnnotation(val description: String =
    """Enter text using Markdown syntax to format the content.
Supported elements include:

- **Headings**: Use `#` for headings `##` for sub headings up to `######`.
- **Bold Text**: Wrap text in double asterisks (e.g., `**bold text**`).
- **Italic Text**: Wrap text in single asterisks (e.g., `*italic text*`).
- **Bold Italic Text**: Wrap text in triple asterisks (e.g., `***bold italic text***`).
- **Strikethrough**: Wrap text in double tildes (e.g., `~~strikethrough~~`).
- Also you can simulate special characters like **(c)** for `(c)` and **(tm)** for `(tm)` and **(r)** for `(r)`.
""")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HexColorAnnotation(val description: String =
    """Enter a hex color value to specify the a color.
The color should be in the format '#RRGGBB' or '#RGB':
            
- **Full format**: '#FF5733' (where 'FF' is red, '57' is green, and '33' is blue).
- **Short format**: '#F53' (where 'F5' is red, '3' is green and blue, equalizing their values).
            
Ensure the hex string starts with '#' and contains valid hexadecimal characters (0-9, A-F).
Example: For a vibrant orange color, use '#FF5733'.

You can also use one of these predefined colors. 
**primary, onPrimary, primaryContainer, onPrimaryContainer, surface, onSurface, secondary, onSecondary, secondaryContainer, onSecondaryContainer, tertiary, onTertiary, tertiaryContainer, onTertiaryContainer, outline, outlineVariant, onErrorContainer, onError, inverseSurface, inversePrimary, inverseOnSurface, background, error, scrim**
""")






@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PaddingAnnotation(val description: String =
    """Enter padding values in the format 'top right bottom left'.
You can also provide one to four values:

- **One value** (e.g., '16'): Applies the same padding to all sides.
- **Two values** (e.g., '8 16'): Applies the first value to the top and bottom, and the second value to the left and right.
- **Three values** (e.g., '8 16 32'): Applies the first value to the top, the second to the left and right, and the third to the bottom.
- **Four values** (e.g., '8 16 32 48'): Applies values to the top, right, bottom, and left in that order.
            
Example: For '8 16 32 48', padding will be set as:
- Top: 8
- Right: 16
- Bottom: 32
- Left: 48
""")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class WeightAnnotation(val description: String = "Enter a weight value as a number. This value represents the relative size or space that the component will occupy within its parent layout. For example, a weight of **1** means the component will take one part of the available space. If you set another component with a weight of **2**, it will take twice as much space as this component.")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntAnnotation(val description: String = "Enter a number without putting it in quotes like **amount: 16**")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class StringAnnotation(val description: String = "Enter a string and put it in quotes like **name: \"Anna\"**")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class LinkAnnotation(val description: String = "At the moment we support two types of link.\nweb -> opens a website like **link: \"web:http://www.example.com\"**\npage -> opens a page like **link: \"page:about\"**")


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ChildrenAnnotation(
    val description: String
)