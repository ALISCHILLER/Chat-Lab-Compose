plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}


subprojects {
    tasks.register("validateModuleBoundaries") {
        group = "verification"
        description = "Fail if forbidden inter-module dependencies are introduced."

        doLast {
            val gradleFile = project.file("build.gradle.kts")
            if (!gradleFile.exists()) return@doLast

            val content = gradleFile.readText()
            val path = project.path

            if (path.startsWith(":feature:") && content.contains("project(\":core:core-storage\")")) {
                error("Forbidden dependency detected: $path must not depend on :core:core-storage")
            }
            if (path.startsWith(":protocol:") && content.contains("project(\":core:core-data\")")) {
                error("Forbidden dependency detected: $path must not depend on :core:core-data")
            }
        }
    }
}

tasks.register("validateArchitecture") {
    group = "verification"
    description = "Validates module boundary rules across all subprojects."
    dependsOn(subprojects.map { it.tasks.named("validateModuleBoundaries") })
}

