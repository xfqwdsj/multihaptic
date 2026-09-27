import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.ksp)
    signing
}

kotlin {
    explicitApi()

    macosArm64()
    iosSimulatorArm64()
    iosX64()
    iosArm64()
    tvosSimulatorArm64()
    tvosArm64()
    applyDefaultHierarchyTemplate()
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/macosArm64/macosArm64Main/kotlin")
            dependencies {
                api(libs.dslUtilities)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-sensitive-resolution")
    }
}

tasks.configureEach {
    if (name.startsWith("ksp") && name != "kspKotlinMacosArm64") {
        dependsOn("kspKotlinMacosArm64")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
    if (name != "kspKotlinMacosArm64") {
        dependsOn("kspKotlinMacosArm64")
    }
}

dependencies {
    add("kspMacosArm64", libs.dslUtilities.ksp)
}

tasks.matching { it.name == "sourcesJar" || it.name.endsWith("SourcesJar") }.configureEach {
    dependsOn("kspKotlinMacosArm64")
}

// Generate shared DSL sources from macosArm64 and expose them to commonMain.
// Sources: https://github.com/google/ksp/issues/1525#issuecomment-4197658943
//          https://github.com/google/ksp/issues/1525#issuecomment-4535778859
afterEvaluate {
    val generatedSources =
        layout.buildDirectory.dir("generated/ksp/macosArm64/macosArm64Main/kotlin").get().asFile.canonicalFile
    val macosArm64Main = kotlin.sourceSets.getByName("macosArm64Main").kotlin
    macosArm64Main.setSrcDirs(
        macosArm64Main.srcDirs.filterNot { it.canonicalFile == generatedSources },
    )
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
