import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

compose.resources {
    packageOfResClass = "com.test.kmp.octoboard.feature.repodetail.generated.resources"
}

sqldelight {
    databases {
        create("FavoritesDatabase") {
            packageName.set("com.test.kmp.octoboard.feature.repodetail.data.local")
        }
    }
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = "com.test.kmp.octoboard.feature.repodetail"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }

        androidResources.enable = true

        withHostTest {}
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
            implementation(project(":core:network"))
            implementation(project(":core:github-api"))
            implementation(project(":core:database"))
            implementation(project(":core:designsystem"))
            implementation(project(":core:ui-nav"))

            implementation(libs.sqldelight.coroutines.extensions)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material.icons.core)

            implementation(libs.ktor.client.core)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
        }
    }
}

dependencies {
    "commonMainImplementation"(platform(libs.koin.bom))
}
