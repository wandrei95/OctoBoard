import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("FavoritesDatabase") {
            packageName.set("com.test.kmp.octoboard.feature.favorites.data.local")
        }
    }
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.test.kmp.octoboard.feature.favorites.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":feature:favorites:entities"))
            implementation(project(":shared:entities"))
            implementation(project(":shared:github-api"))
            implementation(project(":core:network"))
            implementation(project(":core:database"))

            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

dependencies {
    "commonMainImplementation"(platform(libs.koin.bom))
}
