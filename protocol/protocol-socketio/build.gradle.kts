plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab.protocol.socketio"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-protocol-api"))
    implementation(project(":core:core-data")) // For ProtocolBinding

    implementation(libs.kotlinx.coroutines.core)

    // Socket.IO
    implementation(libs.socketio.client)
    implementation(libs.okhttp)

    // Koin for DI
    implementation(libs.koin.core)
}
