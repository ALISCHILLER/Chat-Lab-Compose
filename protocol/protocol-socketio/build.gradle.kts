plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab.protocol.socketio"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
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
    implementation(libs.kotlinx.coroutines.core)

    // Socket.IO
    implementation("io.socket:socket.io-client:2.1.0")

    // Koin for DI
    implementation(libs.koin.android)
}
