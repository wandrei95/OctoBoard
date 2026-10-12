import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
}

sqldelight {
    databases {
        create("FavoritesDatabase") {
            packageName.set("com.test.kmp.octoboard.shared.favoritesdata.local")
        }
    }
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.test.kmp.octoboard.shared.favoritesdata"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:database"))

            implementation(libs.sqldelight.coroutines.extensions)
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}

dependencies {
    "commonMainImplementation"(platform(libs.koin.bom))
}
