plugins {
    id("com.msa.chatlab.android.library")
}

android {
    namespace = "com.msa.chatlab.protocol.signalr"
}

dependencies {
    implementation(project(":core:core-protocol-api"))
    implementation(libs.signalr)
    implementation(libs.slf4j.android)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.timber)
}
