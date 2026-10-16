rootProject.name = "OctoBoard"

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":core:common")
include(":core:network")
include(":core:database")
include(":core:designsystem")
include(":core:ui-nav")

include(":shared:entities")
include(":shared:favorites-data")
include(":shared:github-api")
include(":shared:ui")

include(":feature:search")
include(":feature:repodetail")
include(":feature:favorites")
include(":feature:userprofile")
include(":feature:auth")

include(":integration")

include(":androidApp")
