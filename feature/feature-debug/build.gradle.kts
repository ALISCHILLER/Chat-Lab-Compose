plugins {
    id("com.msa.chatlab.android.library.compose")
}

android {
    namespace = "com.msa.chatlab.feature.debug"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-designsystem"))
    implementation(project(":core:core-observability"))
    implementation(project(":core:core-protocol-api"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
