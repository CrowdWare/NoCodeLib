ElementDefinition {
    name: "Part"
    plugin: "Epub3"
    description: "It's a reference to a markdown file. Which can be referenced in a MarkdownElement."

    AllowedRoots {
        Root { name: "App"}
    }

    Properties {
        Property {
            name: "src"
            type: "String"
            default: ""
            description: "The name of a Markdown file. Example: home.md"
        }
    }
}