plugins {
    id("com.msa.chatlab.android.library")
}

android {
    namespace = "com.msa.chatlab.core.storage"
}

dependencies {
    implementation(project(":core:core-domain"))
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.core)
}
