plugins {
    id("com.msa.chatlab.android.library")
}

android {
    namespace = "com.msa.chatlab.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.koin.core)
}
