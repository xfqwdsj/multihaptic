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
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    macosX64()
    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosX64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    mingwX64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":multihaptic-core"))
            }
        }

        val supportedMain by creating {
            dependsOn(commonMain)
        }

        val unsupportedMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(unsupportedMain)
        }

        val androidMain by getting {
            dependsOn(supportedMain)
            dependencies {
                implementation(project(":multihaptic-platform-dsl"))
            }
        }

        val appleMain by getting {
            dependsOn(supportedMain)
        }

        val appleCoreHapticsMain by creating {
            dependsOn(appleMain)
            dependencies {
                implementation(project(":multihaptic-platform-dsl"))
            }
        }

        val macosMain by getting {
            dependsOn(appleCoreHapticsMain)
        }

        val iosMain by getting {
            dependsOn(appleCoreHapticsMain)
        }

        val tvosMain by getting {
            dependsOn(appleCoreHapticsMain)
        }

        val linuxMain by getting {
            dependsOn(supportedMain)
        }

        val mingwMain by getting {
            dependsOn(unsupportedMain)
        }

        val browserMain by creating {
            dependsOn(supportedMain)
        }

        val jsMain by getting {
            dependsOn(browserMain)
        }

        val wasmJsMain by getting {
            dependsOn(browserMain)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

android {
    namespace = "top.ltfan.multihaptic"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }

    sourceSets {}

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

group = "top.ltfan.multihaptic"
