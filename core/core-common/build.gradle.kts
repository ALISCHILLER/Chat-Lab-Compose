plugins {
    id("com.msa.chatlab.android.library.compose")
}

android {
    namespace = "com.msa.chatlab.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)

    // Add dependency to the design system module
    implementation(project(":core:core-designsystem"))
}
