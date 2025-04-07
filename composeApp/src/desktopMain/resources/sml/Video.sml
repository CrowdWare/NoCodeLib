ElementDefinition {
    name: "Video"
    description: "Displays a video player with local or streamed content."

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
            description: "Path or URL to the video file (e.g. 'video/tutorial.mp4')."
        }
        Property {
            name: "autoplay"
            type: "String"
            default: "false"
            description: "If 'true', the video plays automatically."
        }
        Property {
            name: "controls"
            type: "String"
            default: "true"
            description: "If 'true', playback controls are shown."
        }
        Property {
            name: "loop"
            type: "String"
            default: "false"
            description: "If 'true', the video loops after finishing."
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