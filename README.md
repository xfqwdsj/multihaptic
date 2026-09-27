# multihaptic

![Maven Central Version](https://img.shields.io/maven-central/v/top.ltfan.multihaptic/multihaptic-core) [![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/xfqwdsj/multihaptic)

A Kotlin multiplatform library for haptic feedback across multiple platforms.

## Getting Started

The `multihaptic` library contains these modules:

- `multihaptic-core`: Core functionality for haptic feedback.
- `multihaptic-compose`: Haptic feedback support for Compose.
- `multihaptic-android-dsl`: Android vibration effect DSL.
- `multihaptic-apple-corehaptics-dsl`: Core Haptics DSL with a shared Kotlin model and conversion to Apple Core Haptics
  objects.

Add the modules you use to the matching source sets in your Kotlin Multiplatform project's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("top.ltfan.multihaptic:multihaptic-core:<version>") // Core API, also exposed by the Compose module
            implementation("top.ltfan.multihaptic:multihaptic-compose:<version>") // For Compose support
        }
        androidMain.dependencies {
            implementation("top.ltfan.multihaptic:multihaptic-android-dsl:<version>")
        }
        iosMain.dependencies {
            implementation("top.ltfan.multihaptic:multihaptic-apple-corehaptics-dsl:<version>")
        }
        macosMain.dependencies {
            implementation("top.ltfan.multihaptic:multihaptic-apple-corehaptics-dsl:<version>")
        }
        tvosMain.dependencies {
            implementation("top.ltfan.multihaptic:multihaptic-apple-corehaptics-dsl:<version>")
        }
    }
}
```

Or if you are using Gradle Version Catalogs, add the following to your `gradle/libs.versions.toml`:

```toml
[versions]
multihaptic = "<version>"

[libraries]
multihaptic = { module = "top.ltfan.multihaptic:multihaptic-core", version.ref = "multihaptic" }
multihaptic-compose = { module = "top.ltfan.multihaptic:multihaptic-compose", version.ref = "multihaptic" }
multihaptic-androidDsl = { module = "top.ltfan.multihaptic:multihaptic-android-dsl", version.ref = "multihaptic" }
multihaptic-appleCoreHapticsDsl = { module = "top.ltfan.multihaptic:multihaptic-apple-corehaptics-dsl", version.ref = "multihaptic" }
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
        spinFallback // Sets the predefined fallback to PrimitiveType.Spin

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

| Platform                    | Predefined Effects | Advanced Custom Effects | Details                                                          |
|-----------------------------|--------------------|-------------------------|------------------------------------------------------------------|
| Android (API 21+)           | ✅ Supported       | ✅ Supported            | Uses multiple vibration APIs; custom effects depend on API level |
| iOS (Core Haptics, 15.0+)   | ✅ Supported       | ✅ Supported            | Core Haptics on compatible hardware                              |
| iOS (UIKit, 15.0+)          | ✅ Supported       | 🚫 Fallback             | UIFeedbackGenerator for predefined feedback                      |
| macOS (Core Haptics, 12.0+) | ✅ Supported       | ✅ Supported            | Apple Silicon; Core Haptics on compatible hardware               |
| macOS (AppKit, 12.0+)       | ✅ Supported       | 🚫 Fallback             | Apple Silicon; AppKit feedback for predefined effects            |
| watchOS (8.0+)              | ✅ Supported       | 🚫 Fallback             | WatchKit haptic types, mapped to predefined effects              |
| tvOS (15.0+)                | ✅ Supported       | ✅ Supported            | Core Haptics on compatible hardware                              |
| Browser (Web/Js/Wasm)       | ✅ Supported       | 🚫 Fallback             | Web Vibration API, only duration-based vibration                 |
| Windows                     | 🚫 No effect       | 🚫 No effect            |                                                                  |
| Linux                       | 🚫 No effect       | 🚫 No effect            |                                                                  |

**Note:**

- Advanced custom effects require Core Haptics (Apple) or high Android API level.
- Browser playback uses the Web Vibration API and converts effects to duration-based patterns.
- On backends that use predefined feedback for custom effects, the configured predefined fallback is played.

## Contributing

We welcome contributions! Please submit issues or pull requests for any bugs, features, or improvements.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
