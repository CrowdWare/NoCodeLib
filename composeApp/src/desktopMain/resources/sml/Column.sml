ElementDefinition {
    name: "Column"
    description: "A vertical layout container. It arranges its children from top to bottom."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Row" }
        Root { name: "Column" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "verticalArrangement"
            type: "String"
            default: "Top"
            description: "Arrangement of children along the vertical axis. Options: Top, Center, Bottom, SpaceBetween, SpaceAround, SpaceEvenly."
        }

        Property {
            name: "horizontalAlignment"
            type: "String"
            default: "Start"
            description: "Alignment of children along the horizontal axis. Options: Start, CenterHorizontally, End."
        }

        Property {
            name: "padding"
            type: "String"
            default: ""
            description: "Space around the content. Accepts 1, 2 or 4 integers (in dp): '8' (all), '8 16' (vertical horizontal), or '8 16 16 8' (top right bottom left)."
        }

        Property {
            name: "background"
            type: "String"
            default: ""
            description: "Optional background color for the Column. Example: '#FFFFFF'."
        }
    }
}