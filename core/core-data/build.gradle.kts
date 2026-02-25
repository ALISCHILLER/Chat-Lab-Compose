plugins {
    id("com.msa.chatlab.android.library")
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab.core.data"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // DataStore for active profile persistence
    implementation(libs.androidx.datastore.preferences)

    // modules
    implementation(project(":core:core-common"))
    implementation(project(":core:core-domain"))
    implementation(project(":core:core-storage"))
    implementation(project(":core:core-protocol-api"))
    implementation(project(":core:core-observability"))
}
