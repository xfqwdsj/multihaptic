# multihaptic

![Maven Central Version](https://img.shields.io/maven-central/v/top.ltfan.multihaptic/multihaptic-core) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/multihaptic)

A Kotlin multiplatform library for haptic feedback across multiple platforms.

## Getting Started

The `multihaptic` library contains these modules:

- `multihaptic-core`: Core functionality for haptic feedback.
- `multihaptic-compose`: Haptic feedback support for Compose.
- `multihaptic-platform-dsl`: Platform-specific DSL for haptic feedback.

To use `multihaptic` in your Kotlin Multiplatform project, add the following dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("top.ltfan.multihaptic:multihaptic-core:<version>") // Not needed if you add the Compose dependency
    implementation("top.ltfan.multihaptic:multihaptic-compose:<version>") // For Compose support

    implementation("top.ltfan.multihaptic:multihaptic-platform-dsl:<version>") // If you want to use the DSLs to build the haptic effects
}
```

Or if you are using Gradle Version Catalogs, add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
multihaptic = "<version>"

[libraries]
ltmath = { module = "top.ltfan.multihaptic:multihaptic-core", version.ref = "multihaptic" }
multihaptic-compose = { module = "top.ltfan.multihaptic:multihaptic-compose", version.ref = "multihaptic" }
multihaptic-platformDsl = { module = "top.ltfan.multihaptic:multihaptic-platform-dsl", version.ref = "multihaptic" }
```

Make sure your `settings.gradle.kts` includes the repository:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

## Features

### Predefined Haptic Effects (Core)

Inspired by Android's
[`VibrationEffect.Composition`](https://developer.android.com/reference/kotlin/android/os/VibrationEffect.Composition),
`multihaptic` provides a set of predefined haptic effects that can be used across platforms.

You can create predefined haptic effects by following this example:

```kotlin
HapticEffect {
    predefined(PrimitiveType.Click) {
        scale = .5f // Adjust the intensity of the click
    }
    predefined(PrimitiveType.Thud) {
        scale = .8f // Adjust the intensity of the thud
        delay = 100.milliseconds // Delay before the thud effect
        delayType = DelayType.Pause // 100 milliseconds pause after the previous effect
    }
    predefined(PrimitiveType.Tick) {
        scale = .3f // Adjust the intensity of the tick
        delay = 50.milliseconds // Delay before the tick effect
        delayType = DelayType.RelativeStartOffset // 50 milliseconds relative to the start of the previous effect
    }
    lowTick {
        delay = 30.milliseconds
    }
    quickRise // With default parameters
}
```

### Custom Haptic Effects (Core)

You can also create custom haptic effects:

```kotlin
HapticEffect {
    custom {
        fallback = PrimitiveType.Spin
        // or
        spinFallback

        curves {
            intensity {
                0f at 0.milliseconds // Start with no intensity
                1f at 5.milliseconds // Increase to full intensity
                0f at 10.milliseconds // Decrease back to no intensity
            }
            sharpness {
                .3f at 0.milliseconds // Start with low sharpness
            }
        }
    }
}
```

### Compose Support (Compose)

The `multihaptic-compose` module provides support for haptic feedback in Compose Multiplatform applications. You can use
the `rememberVibrator` from a Composable function to get a `Vibrator` instance and trigger haptic effects.

## Platforms

| Platform                            | Predefined Effects | Advanced Custom Effects | Details                                                                              |
|-------------------------------------|--------------------|-------------------------|--------------------------------------------------------------------------------------|
| Android                             | ✅ Supported        | ✅ Supported             | Uses multiple vibration API; advanced composition/custom effects depend on API level |
| iOS (CoreHaptics, 13.0+)            | ✅ Supported        | ✅ Supported             | Core Haptics for complex custom effects                                              |
| iOS (UIKit, 10.0+)                  | ✅ Supported        | 🚫 Not Supported        | UIFeedbackGenerator for predefined feedback only                                     |
| macOS (CoreHaptics/Catalyst, 13.0+) | ✅ Supported        | ✅ Supported             | Core Haptics available for Mac Catalyst apps                                         |
| macOS (AppKit, 10.11+)              | ✅ Supported        | 🚫 Not Supported        | AppKit Haptic Feedback, only preset effects                                          |
| watchOS                             | ✅ Supported        | 🚫 Not Supported        | WatchKit haptic types, mapped to predefined effects                                  |
| tvOS (14.0+)                        | ✅ Supported        | ✅ Supported             | Core Haptics if available                                                            |
| Browser (Web/JS/WASM)               | ✅ Supported        | 🚫 Not Supported        | Web Vibration API, only duration-based vibration                                     |
| Windows                             | 🚫 No effect       | 🚫 No effect            |                                                                                      |
| Linux                               | 🚫 No effect       | 🚫 No effect            |                                                                                      |

**Note:**

- Advanced custom effects require Core Haptics (Apple) or high Android API level.
- Browsers only support basic vibration, not custom curves.
- If predefined effects are supported but advanced custom effects are not, the library will use the predefined effects
  as a fallback.

## Contributing

We welcome contributions! Please submit issues or pull requests for any bugs, features, or improvements.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
