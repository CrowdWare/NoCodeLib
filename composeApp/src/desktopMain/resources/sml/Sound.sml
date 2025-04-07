ElementDefinition {
    name: "Sound"
    description: "Plays an audio file, either automatically or interactively."

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
            description: "Path to the sound file, e.g. 'sounds/intro.mp3'."
        }
        Property {
            name: "autoplay"
            type: "String"
            default: "false"
            description: "If 'true', the sound will play automatically."
        }
        Property {
            name: "loop"
            type: "String"
            default: "false"
            description: "If 'true', the sound will loop continuously."
        }
    }
}