import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    android {
        namespace = "top.ltfan.multihaptic"
        compileSdk {
            version = release(37) {
                minorApiLevel = 2
            }
        }
        minSdk = 21

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    tvosSimulatorArm64()
    tvosArm64()
    mingwX64()
    js {
        browser()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":multihaptic-core"))
            }
        }

        val supportedMain = create("supportedMain") {
            dependsOn(commonMain.get())
        }

        val unsupportedMain = create("unsupportedMain") {
            dependsOn(commonMain.get())
        }

        jvmMain {
            dependsOn(unsupportedMain)
        }

        androidMain {
            dependsOn(supportedMain)
            dependencies {
                implementation(project(":multihaptic-android-dsl"))
            }
        }

        appleMain {
            dependsOn(supportedMain)
        }

        val appleCoreHapticsMain = create("appleCoreHapticsMain") {
            dependsOn(appleMain.get())
            dependencies {
                implementation(project(":multihaptic-apple-corehaptics-dsl"))
            }
        }

        macosMain {
            dependsOn(appleCoreHapticsMain)
        }

        iosMain {
            dependsOn(appleCoreHapticsMain)
        }

        tvosMain {
            dependsOn(appleCoreHapticsMain)
        }

        linuxMain {
            dependsOn(supportedMain)
        }

        mingwMain {
            dependsOn(unsupportedMain)
        }

        val browserMain = create("browserMain") {
            dependsOn(supportedMain)
        }

        jsMain {
            dependsOn(browserMain)
        }

        wasmJsMain {
            dependsOn(browserMain)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

group = "top.ltfan.multihaptic"
