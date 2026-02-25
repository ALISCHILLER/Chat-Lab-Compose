plugins {
    id("com.msa.chatlab.android.library.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.msa.chatlab.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.serialization.json)

    // Add dependency to the design system module
    implementation(project(":core:core-designsystem"))
}
