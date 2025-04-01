ElementDefinition {
    name: "AsyncImage"
    description: "Displays an image from a local file or remote URL."

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
            description: "The URL of the image. Example: 'https://example.com/image.jpg'."
        }

        Property {
            name: "weight"
            type: "Int"
            default: "0"
            description: "Determines how much space the image takes relative to other elements in the same layout. Use 0 for no weight-based resizing."
        }

        Property {
            name: "width"
            type: "Int"
            default: "0"
            description: "Fixed width of the image in dp. Use 0 for automatic sizing."
        }

        Property {
            name: "height"
            type: "Int"
            default: "0"
            description: "Fixed height of the image in dp. Use 0 for automatic sizing."
        }

        Property {
            name: "scale"
            type: "String"
            default: ""
            description: "Defines how the image should be scaled within its bounds. Options include: 'Fit', 'Fill', 'Crop', 'Inside'."
        }

        Property {
            name: "alignment"
            type: "String"
            default: "Center"
            description: "How the image is aligned inside its layout container. Options: Start, Center, End, Top, Bottom."
        }
    }
}