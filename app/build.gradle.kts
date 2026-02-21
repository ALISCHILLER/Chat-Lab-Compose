plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.msa.chatlab"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // AndroidX & Navigation
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // DI (Koin)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Room (used by app DI module)
    implementation(libs.room.runtime)

    // Feature modules
    implementation(project(":feature:feature-settings"))
    implementation(project(":feature:feature-lab"))
    implementation(project(":feature:feature-connect"))
    implementation(project(":feature:feature-chat"))
    implementation(project(":feature:feature-debug"))

    // Protocol modules
    implementation(project(":protocol:protocol-websocket-okhttp"))
    implementation(project(":protocol:protocol-websocket-ktor"))
    implementation(project(":protocol:protocol-socketio"))
    implementation(project(":protocol:protocol-signalr"))
    implementation(project(":protocol:protocol-mqtt"))

    // Core modules
    implementation(project(":core:core-designsystem"))
    implementation(project(":core:core-data"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-protocol-api"))
    implementation(project(":core:core-storage"))
    implementation(project(":core:core-observability"))
    implementation(project(":core:core-nativebridge"))

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
}