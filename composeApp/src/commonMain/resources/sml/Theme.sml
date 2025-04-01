ElementDefinition {
    name: "Theme"
    description: "It is a color palette definition, typically used to customize the overall look and feel of the app."

    AllowedRoots {
        Root { name: "App" }
    }

    Properties {
        Property {
            name: "primary"
            type: "String"
            default: ""
            description: "Primary color of the theme."
        }
        Property {
            name: "onPrimary"
            type: "String"
            default: ""
            description: "Color for content on top of the primary color."
        }
        Property {
            name: "primaryContainer"
            type: "String"
            default: ""
            description: "Background container using the primary color."
        }
        Property {
            name: "onPrimaryContainer"
            type: "String"
            default: ""
            description: "Content color on top of the primary container."
        }
        Property {
            name: "secondary"
            type: "String"
            default: ""
            description: "Secondary color used for less prominent components."
        }
        Property {
            name: "onSecondary"
            type: "String"
            default: ""
            description: "Color for content on top of the secondary color."
        }
        Property {
            name: "secondaryContainer"
            type: "String"
            default: ""
            description: "Background container using the secondary color."
        }
        Property {
            name: "onSecondaryContainer"
            type: "String"
            default: ""
            description: "Content color on top of the secondary container."
        }
        Property {
            name: "tertiary"
            type: "String"
            default: ""
            description: "Tertiary color used for accents and highlights."
        }
        Property {
            name: "onTertiary"
            type: "String"
            default: ""
            description: "Color for content on top of the tertiary color."
        }
        Property {
            name: "tertiaryContainer"
            type: "String"
            default: ""
            description: "Background container using the tertiary color."
        }
        Property {
            name: "onTertiaryContainer"
            type: "String"
            default: ""
            description: "Content color on top of the tertiary container."
        }
        Property {
            name: "error"
            type: "String"
            default: ""
            description: "Error color used to indicate problems."
        }
        Property {
            name: "errorContainer"
            type: "String"
            default: ""
            description: "Background container for error states."
        }
        Property {
            name: "onError"
            type: "String"
            default: ""
            description: "Color for content on top of the error color."
        }
        Property {
            name: "onErrorContainer"
            type: "String"
            default: ""
            description: "Content color on top of the error container."
        }
        Property {
            name: "background"
            type: "String"
            default: ""
            description: "App background color."
        }
        Property {
            name: "onBackground"
            type: "String"
            default: ""
            description: "Color for content on top of the background."
        }
        Property {
            name: "surface"
            type: "String"
            default: ""
            description: "Color for surfaces such as cards and sheets."
        }
        Property {
            name: "onSurface"
            type: "String"
            default: ""
            description: "Color for content on top of the surface."
        }
        Property {
            name: "surfaceVariant"
            type: "String"
            default: ""
            description: "Variant of the surface color for differentiation."
        }
        Property {
            name: "onSurfaceVariant"
            type: "String"
            default: ""
            description: "Color for content on top of the surface variant."
        }
        Property {
            name: "outline"
            type: "String"
            default: ""
            description: "Color for outlines and borders."
        }
        Property {
            name: "inverseOnSurface"
            type: "String"
            default: ""
            description: "Content color on inverse surfaces."
        }
        Property {
            name: "inverseSurface"
            type: "String"
            default: ""
            description: "Inverse background surface color."
        }
        Property {
            name: "inversePrimary"
            type: "String"
            default: ""
            description: "Inverse of the primary color, used in dark themes."
        }
        Property {
            name: "surfaceTint"
            type: "String"
            default: ""
            description: "Tint color applied to elevated surfaces."
        }
        Property {
            name: "outlineVariant"
            type: "String"
            default: ""
            description: "A variant color used for subtle outlines."
        }
        Property {
            name: "scrim"
            type: "String"
            default: ""
            description: "Scrim color used behind modals and dialogs."
        }
    }
}