plugins {
    id("com.msa.chatlab.android.library.compose")
}

android {
    namespace = "com.msa.chatlab.feature.lab"
}

dependencies {
    implementation(project(":core:core-data"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-designsystem"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-storage"))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
