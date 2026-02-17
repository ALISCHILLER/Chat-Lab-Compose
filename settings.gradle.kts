pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}

rootProject.name = "ChatLab"

include(
    ":app",

    // Core
    ":core:core-common",
    ":core:core-domain",
    ":core:core-protocol-api",
    ":core:core-data",
    ":core:core-storage",
    ":core:core-observability",
    ":core:core-nativebridge",

    // Feature
    ":feature:feature-settings",
    ":feature:feature-lab",
    ":feature:feature-connect",
    ":feature:feature-chat",
    ":feature:feature-debug",

    // Protocols
    ":protocol:protocol-websocket-okhttp",
    ":protocol:protocol-websocket-ktor",
    ":protocol:protocol-mqtt",
    ":protocol:protocol-socketio",
    ":protocol:protocol-signalr"
)
