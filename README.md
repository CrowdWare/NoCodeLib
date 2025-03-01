# NoCodeLib

A shared library for NoCode applications, providing common UI components, utilities, and models.

## Overview

NoCodeLib is a Kotlin Multiplatform library that provides the core functionality for NoCode applications like FreeBookDesigner and NoCodeDesigner. It includes:

- UI components for desktop applications
- Theme management
- Utilities for file handling and parsing
- View models for state management
- Common models for application data

## Usage

To use NoCodeLib in your project, add it as a dependency:

```kotlin
// In your build.gradle.kts
dependencies {
    implementation("at.crowdware:nocodelib:1.0.0")
}
```

Or add it as a Git submodule:

```bash
git submodule add https://github.com/crowdware/nocodelib.git
```

## License

NoCodeLib is licensed under the GNU General Public License v3.0.
