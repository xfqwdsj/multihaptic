import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    signing
}

kotlin {
    explicitApi()

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    android {
        namespace = "top.ltfan.multihaptic.compose"
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
    iosArm64()
    linuxX64()
    linuxArm64()
    watchosSimulatorArm64()
    watchosArm32()
    tvosArm64()
    mingwX64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain {
            dependencies {
                api(project(":multihaptic-core"))
                implementation(libs.compose.runtime)
            }
        }

        val configUnneededMain = create("configUnneededMain") {
            dependsOn(commonMain.get())
        }

        val supportedMain = create("supportedMain") {
            dependsOn(commonMain.get())
        }

        val unsupportedMain = create("unsupportedMain") {
            dependsOn(commonMain.get())
            dependsOn(configUnneededMain)
        }

        jvmMain {
            dependsOn(unsupportedMain)
        }

        androidMain {
            dependsOn(supportedMain)
            dependencies {
                implementation(libs.compose.ui)
            }
        }

        appleMain {
            dependsOn(supportedMain)
            dependsOn(configUnneededMain)
        }

        val appleCoreHapticsMain = create("appleCoreHapticsMain") {
            dependsOn(appleMain.get())
        }

        macosMain {
            dependsOn(appleCoreHapticsMain)
        }

        iosMain {
            dependsOn(appleCoreHapticsMain)
        }

        linuxMain {
            dependsOn(supportedMain)
            dependsOn(configUnneededMain)
        }

        mingwMain {
            dependsOn(unsupportedMain)
            dependsOn(configUnneededMain)
        }

        val browserMain = create("browserMain") {
            dependsOn(supportedMain)
            dependsOn(configUnneededMain)
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
        ),
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
