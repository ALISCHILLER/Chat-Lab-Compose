plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab.protocol.signalr"
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
    implementation(project(":core:core-data"))
    implementation(project(":core:core-common")) // For AppScope

    implementation(libs.kotlinx.coroutines.core)

    // SignalR
    implementation(libs.signalr)
    implementation(libs.slf4j.android)

    // OkHttp for transport
    implementation(libs.okhttp)

    // Koin for DI
    implementation(libs.koin.core)
}
