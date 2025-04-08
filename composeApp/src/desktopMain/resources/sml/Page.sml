ElementDefinition {
    name: "Page"
    description: "Base for the NoCodeDesigner page"

    AllowedRoots {
        Root { name: "App"}
    }
    
    Properties {
        Property {
            name: "smlVersion"
            type: "String"
            default: "1.1"
            description: "The version of the sml dialect."
        }
        Property {
             name: "background"
             type: "String"
             default: "background"
             description: "The color of the background as hexvalue or colorname. Example: '#FFFFFF'"
        }
        Property {
            name: "title"
            type: "String"
            default: ""
            description: "The name of the page."
        }
        Property {
            name: "scrollable"
            type: "String"
            default: "false"
            description: "Determines if the content of the page can be scrolled or not. Values are true|false"
        }
        Property {
            name: "padding"
            type: "String"
            default: ""
            description: "Space around the content. Accepts 1, 2 or 4 integers (in dp): '8' (all), '8 16' (vertical horizontal), or '8 16 16 8' (top right bottom left)."
        }
    }
}