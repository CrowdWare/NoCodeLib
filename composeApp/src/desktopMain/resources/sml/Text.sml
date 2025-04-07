ElementDefinition {
    name: "Text"
    description: "Renders normal text."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "text"
            type: "String"
            default: ""
            description: "The text content to render."
        }
        Property {
            name: "color"
            type: "String"
            default: "#000000"
            description: "Text color. Example: '#333333' or color name like 'onBackground'."
        }
        Property {
            name: "fontSize"
            type: "Int"
            default: "14"
            description: "Font size in sp."
        }
        Property {
            name: "fontWeight"
            type: "String"
            default: "Normal"
            description: "Font weight. Example: Normal, Bold, Medium, etc."
        }
        Property {
            name: "textAlign"
            type: "String"
            default: "Left"
            description: "Text alignment: Left, Center, Right, Justify."
        }
        Property {
            name: "padding"
            type: "String"
            default: ""
            description: "Space around the content. Accepts 1, 2 or 4 integers (in dp): '8' (all), '8 16' (vertical horizontal), or '8 16 16 8' (top right bottom left)."
        }
    }
}