ElementDefinition {
    name: "Youtube"
    description: "Embeds a YouTube video player with a specific video ID."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
        Property {
            name: "videoId"
            type: "String"
            default: ""
            description: "The unique YouTube video ID. Example: 'dQw4w9WgXcQ'."
        }
        Property {
            name: "autoplay"
            type: "String"
            default: "false"
            description: "If 'true', playback starts automatically."
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