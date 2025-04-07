ElementDefinition {
    name: "Markdown"
    description: "Renders styled text using the Markdown format, with optional inline images."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "part"
            type: "String"
            default: ""
            description: "The name of the part, where the markdown can be loaded from a file. For example: home.md which normally will be stored in the parts folders. Like parts-de|parts-en. The part for the actual language will be loaded automatically. That means in case of a mobile app the markdown will be loaded from parts-es in case Spanish is the selected language and parts-en if the selected language is English."
        }
        Property {
            name: "text"
            type: "String"
            default: ""
            description: "The Markdown content to render. Images using '![]()' are supported."
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
            name: "paddingStart"
            type: "Int"
            default: "0"
            description: "Start padding in dp."
        }
        Property {
            name: "paddingTop"
            type: "Int"
            default: "0"
            description: "Top padding in dp."
        }
        Property {
            name: "paddingBottom"
            type: "Int"
            default: "0"
            description: "Bottom padding in dp."
        }
    }
}