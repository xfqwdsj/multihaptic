import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ksp)
    signing
}

kotlin {
    explicitApi()

    android {
        namespace = "top.ltfan.multihaptic.platform.android.dsl"
        compileSdk {
            version = release(37) {
                minorApiLevel = 2
            }
        }
        minSdk = 21
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            execution = "HOST"
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                api(libs.dslUtilities)
            }
        }
        androidMain {
            kotlin.srcDir("build/generated/ksp/android/androidMain/kotlin")
            dependencies {
                implementation(libs.androidx.core)
                implementation(libs.androidx.annotation)
            }
        }
        getByName("androidDeviceTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.runner)
            }
        }
        getByName("androidHostTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

dependencies {
    add("kspAndroid", libs.dslUtilities.ksp)
}

tasks.matching { it.name == "sourcesJar" || it.name.endsWith("SourcesJar") }.configureEach {
    dependsOn("kspAndroidMain")
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
