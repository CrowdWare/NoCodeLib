ElementDefinition {
    name: "LazyColumn"
    description: "A scrollable vertical container that only composes visible items."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
    }

    Properties {
        Property {
            name: "padding"
            type: "String"
            default: ""
            description: "Space around the content. Accepts 1, 2 or 4 integers (in dp): '8' (all), '8 16' (vertical horizontal), or '8 16 16 8' (top right bottom left)."
        }
        Property {
            name: "spacing"
            type: "String"
            default: "0"
            description: "Space between items inside the list (in dp)."
        }
    }
}