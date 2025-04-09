ElementDefinition {
    name: "App"
    description: "Base for the NoCodeDesigner project"

    Properties {
        Property {
            name: "smlVersion"
            type: "String"
            default: "1.1"
            description: "The version of the sml dialect."
        }
        Property {
            name: "version"
            type: "String"
            default: "1.0"
            description: "The version of the app."
        }
        Property {
            name: "name"
            type: "String"
            default: ""
            description: "The name of the app."
        }
        Property {
            name: "description"
            type: "String"
            default: ""
            description: "This describes the project"
        }
        Property {
            name: "icon"
            type: "String"
            default: ""
            description: "An icon used to create as desktop icon"
        }
        Property {
            name: "id"
            type: "String"
            default: ""
            description: "An ID to be used as a unique identifier."
        }
        Property {
            name: "author"
            type: "String"
            default: ""
            description: "The name of the author that creates the project."
        }
    }
}