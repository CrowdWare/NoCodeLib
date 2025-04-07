ElementDefinition {
    name: "Scene"
    description: "Displays a 3D scene or animation. Often used for tutorials, yoga, or martial arts visualizations."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "src"
            type: "String"
            default: ""
            description: "Path to the 3D scene or animation file, e.g. 'scene/yoga1.json'."
        }
        Property {
            name: "width"
            type: "Int"
            default: "0"
            description: "Width in dp. Use 0 for automatic sizing."
        }
        Property {
            name: "height"
            type: "Int"
            default: "0"
            description: "Height in dp. Use 0 for automatic sizing."
        }
    }
}