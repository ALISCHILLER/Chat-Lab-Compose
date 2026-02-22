plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.msa.chatlab.core.data"
    compileSdk = 34
    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    api(project(":core:core-protocol-api"))
    api(project(":core:core-storage"))

    implementation(project(":core:core-common"))

    api(project(":core:core-observability"))
    implementation(libs.kotlinx.coroutines.core)
}
