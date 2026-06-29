package com.example.serviceflow

import android.app.Application
import com.example.serviceflow.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ServiceFlowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ServiceFlowApplication)
            modules(appModule)
        }
    }
}
