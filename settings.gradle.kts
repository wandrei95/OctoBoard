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

include(":androidApp")
include(":core:common")
include(":core:network")
include(":core:database")
include(":core:designsystem")
include(":core:ui-nav")
include(":shared:entities")
include(":shared:github-api")
include(":shared:ui")
include(":feature:search:entities")
include(":feature:search:data")
include(":feature:search:presentation")
include(":feature:repodetail:entities")
include(":feature:repodetail:data")
include(":feature:repodetail:presentation")
include(":feature:favorites:entities")
include(":feature:favorites:data")
include(":feature:favorites:presentation")
include(":feature:userprofile:entities")
include(":feature:userprofile:data")
include(":feature:userprofile:presentation")
include(":feature:auth:data")
include(":feature:auth:presentation")
include(":integration")
