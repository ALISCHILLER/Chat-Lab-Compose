plugins {
    id("com.msa.chatlab.android.library")
}

android {
    namespace = "com.msa.chatlab.protocol.socketio"
}

dependencies {
    implementation(project(":core:core-protocol-api"))
    implementation(libs.socketio.client)
    implementation(libs.koin.core)
    implementation(libs.kotlinx.coroutines.core)
}
