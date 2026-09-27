import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
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

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    android {
        namespace = "top.ltfan.multihaptic.core"
        compileSdk {
            version = release(37) {
                minorApiLevel = 2
            }
        }
        minSdk = 21

        withHostTest {
            isIncludeAndroidResources = true
        }

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
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                api(libs.dslUtilities)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val supportedMain = create("supportedMain") {
            dependsOn(commonMain.get())
        }

        val supportedTest = create("supportedTest") {
            dependsOn(commonTest.get())
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
                implementation(libs.androidx.core)
                implementation(libs.androidx.annotation)
            }
        }

        getByName("androidHostTest") {
            dependsOn(supportedTest)
        }

        appleMain {
            dependsOn(supportedMain)
        }

        appleTest {
            dependsOn(supportedTest)
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

        linuxTest {
            dependsOn(supportedTest)
        }

        mingwMain {
            dependsOn(unsupportedMain)
        }

        val browserMain = create("browserMain") {
            dependsOn(supportedMain)
        }

        val browserTest = create("browserTest") {
            dependsOn(supportedTest)
        }

        jsMain {
            dependsOn(browserMain)
        }

        jsTest {
            dependsOn(browserTest)
        }

        wasmJsMain {
            dependsOn(browserMain)
            dependencies {
                implementation(libs.kotlinx.browser)
            }
        }

        wasmJsTest {
            dependsOn(browserTest)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

tasks.configureEach {
    if (name.startsWith("ksp") && name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.dslUtilities.ksp)
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
