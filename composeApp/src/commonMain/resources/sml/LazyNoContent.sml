ElementDefinition {
    name: "LazyNoContent"
    description: "Fallback UI shown when the LazyColumn or LazyRow is empty."

    AllowedRoots {
        Root { name: "LazyColumn" }
        Root { name: "LazyRow" }
    }

    Properties {
        // Keine Properties notwendig
    }
}