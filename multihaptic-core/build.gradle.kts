import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    signing
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    androidTarget {
        publishLibraryVariants("release")
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
                implementation(libs.kotlinx.coroutines.core)
                api(libs.dslUtilities)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
            }
        }

        val supportedMain by creating {
            dependsOn(commonMain)
        }

        val supportedTest by creating {
            dependsOn(commonTest)
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
                implementation(libs.androidx.core)
                implementation(libs.androidx.annotation)
            }
        }

        val androidUnitTest by getting {
            dependsOn(supportedTest)
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
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
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

dokka {
    dokkaSourceSets {
        configureEach {
            samples.from(rootDir.resolve("samples/src/$name/kotlin"))
            sourceLink {
                remoteUrl = uri("https://github.com/xfqwdsj/multihaptic/tree/v${version}/${project.name}")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()

    pom {
        name = project.name
        description = "A Kotlin multiplatform library for haptic feedback across multiple platforms."
        url = "https://github.com/xfqwdsj/multihaptic"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/license/mit/"
                distribution = "repo"
            }
        }

        developers {
            developer {
                id = "xfqwdsj"
                name = "LTFan"
                email = "xfqwdsj@qq.com"
                roles = listOf("Author", "Maintainer")
            }
        }

        scm {
            connection = "scm:git:https://github.com/xfqwdsj/multihaptic.git"
            developerConnection = "scm:git:https://github.com/xfqwdsj/multihaptic.git"
            url = "https://github.com/xfqwdsj/multihaptic"
        }
    }

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka(tasks.dokkaGeneratePublicationHtml),
        )
    )
}

publishing {
    repositories {
        maven {
            name = "gitHubPackages"
            url = uri("https://maven.pkg.github.com/xfqwdsj/multihaptic")
            credentials(PasswordCredentials::class)
        }
    }
}

signing {
    sign(publishing.publications)
    val publishSigningMode = findProperty("publishSigningMode") as String?
    if (publishSigningMode == "inMemory") return@signing
    useGpgCmd()
}

group = "top.ltfan.multihaptic"
