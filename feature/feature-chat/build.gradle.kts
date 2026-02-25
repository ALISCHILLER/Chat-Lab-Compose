plugins {
    id("com.msa.chatlab.android.library.compose")
}

android {
    namespace = "com.msa.chatlab.feature.chat"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-designsystem"))
    implementation(project(":core:core-common"))
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
