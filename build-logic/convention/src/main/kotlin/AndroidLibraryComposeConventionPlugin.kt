import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // First, apply the base android library convention
            pluginManager.apply("com.msa.chatlab.android.library")

            // Then, configure compose specific things
            extensions.configure<LibraryExtension> { 
                buildFeatures.compose = true
                composeOptions {
                    kotlinCompilerExtensionVersion = "1.5.14"
                }
            }

            // Add Compose dependencies
            dependencies {
                val bom = platform("androidx.compose:compose-bom:2024.02.02")
                add("implementation", bom)
                add("androidTestImplementation", bom)
                add("implementation", "androidx.compose.ui:ui")
                add("implementation", "androidx.compose.ui:ui-graphics")
                add("implementation", "androidx.compose.material3:material3")
                add("implementation", "androidx.compose.ui:ui-tooling-preview")
                add("debugImplementation", "androidx.compose.ui:ui-tooling")
                add("implementation", "androidx.compose.material:material-icons-extended:1.6.7")
                add("implementation", "androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
                // Add any other common compose dependencies here
            }
        }
    }
}
