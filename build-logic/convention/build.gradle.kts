import org.gradle.kotlin.dsl.dependencies

plugins {
    `kotlin-dsl`
}

group = "com.msa.chatlab.buildlogic"

// Register the convention plugins
gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "com.msa.chatlab.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "com.msa.chatlab.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
    }
}

// Configure the dependencies for the convention plugin itself.
dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
}
