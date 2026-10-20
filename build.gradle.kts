plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.spotless)
}

spotless {
    kotlinGradle {
        ktfmt().kotlinlangStyle()
    }
}
