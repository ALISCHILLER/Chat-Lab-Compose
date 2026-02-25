import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.msa.chatlab.android.library")

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")
            val composeCompiler = libs.findVersion("compose-compiler").get().requiredVersion

            extensions.configure<LibraryExtension> {
                buildFeatures.compose = true
                composeOptions {
                    kotlinCompilerExtensionVersion = composeCompiler
                }
            }

            dependencies {
                val composeBom = libs.findLibrary("androidx-compose-bom").get()
                add("implementation", platform(composeBom))
                add("androidTestImplementation", platform(composeBom))

                add("implementation", libs.findLibrary("androidx-compose-ui").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
                add("implementation", libs.findLibrary("androidx-compose-material3").get())
                add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
                add("implementation", libs.findLibrary("androidx-compose-material-icons-extended").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
            }
        }
    }
}
