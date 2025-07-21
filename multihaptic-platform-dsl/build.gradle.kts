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
    tvosSimulatorArm64()
    tvosX64()
    tvosArm64()
    js { browser() }
    @OptIn(ExperimentalWasmDsl::class) wasmJs { browser() }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.dslUtilities)
            }
        }

        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.runner)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.annotation)
            }
        }

        val appleMain by getting

        val appleCoreHapticsMain by creating {
            dependsOn(appleMain)
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
