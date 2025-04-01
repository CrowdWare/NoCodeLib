ElementDefinition {
    name: "Spacer"
    description: "Adds vertical or horizontal space between elements."

    AllowedRoots {
        Root { name: "Page" }
        Root { name: "Column" }
        Root { name: "Row" }
        Root { name: "LazyContent"}
        Root { name: "LazyNoContent"}
    }

    Properties {
         Property {
             name: "amount"
             type: "Int"
             default: "0"
             description: "Dependent of the context its used for width or height."
         }
    }
}