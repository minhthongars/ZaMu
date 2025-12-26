pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "zamu"
include(":app")
include(":feature_home")
include(":core_common")
include(":navigation")
include(":feature_setting")
include(":feature_player")
include(":feature_playlist")
include(":feature_playlist_api")
include(":feature_mashup_api")
