package com.test.kmp.octoboard

import android.app.Application
import com.test.kmp.octoboard.integration.di.commonAppModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class OctoBoardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@OctoBoardApp)
            modules(commonAppModules)
        }
    }
}
