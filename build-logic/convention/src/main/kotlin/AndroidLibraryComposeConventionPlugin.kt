import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.android")

            extensions.configure<LibraryExtension> { 
                buildFeatures.compose = true
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.14"
                }

                dependencies {
                    // Explicitly define Compose BOM dependency
                    val bom = platform("androidx.compose:compose-bom:2024.02.02")
                    add("implementation", bom)
                    add("androidTestImplementation", bom)
                }
            }
        }
    }
}
