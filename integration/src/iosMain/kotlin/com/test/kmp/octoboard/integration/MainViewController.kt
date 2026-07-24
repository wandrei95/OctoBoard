package com.test.kmp.octoboard.integration

import androidx.compose.ui.window.ComposeUIViewController
import com.test.kmp.octoboard.integration.di.commonAppModules
import com.test.kmp.octoboard.integration.image.configureImageLoader
import com.test.kmp.octoboard.integration.navigation.IntegrationRoot
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

fun MainViewController() = run {
    configureImageLoader()
    ComposeUIViewController {
        KoinApplication(koinConfiguration { modules(commonAppModules) }) {
            IntegrationRoot()
        }
    }
}
