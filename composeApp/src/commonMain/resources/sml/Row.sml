ElementDefinition {
    name: "Row"
    description: "A horizontal layout container. It places its children in a single row, from left to right."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "horizontalAlignment"
            type: "String"
            default: "Start"
            description: "Alignment of children along the horizontal axis. Options: Start, Center, End, SpaceBetween, SpaceAround, SpaceEvenly."
        }

        Property {
            name: "verticalAlignment"
            type: "String"
            default: "CenterVertically"
            description: "Alignment of children along the vertical axis. Options: Top, CenterVertically, Bottom."
        }

        Property {
            name: "padding"
            type: "String"
            default: ""
            description: "Space around the content. Accepts 1, 2 or 4 integers (in dp): '8' (all), '8 16' (vertical horizontal), or '8 16 16 8' (top right bottom left)."
        }

        Property {
            name: "spacing"
            type: "String"
            default: "0dp"
            description: "Spacing between child elements. Example: '8dp'."
        }

        Property {
            name: "background"
            type: "String"
            default: ""
            description: "Optional background color for the Row. Example: '#FFFFFF'."
        }
    }
}