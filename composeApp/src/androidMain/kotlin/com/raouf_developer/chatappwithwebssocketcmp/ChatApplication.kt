package com.raouf_developer.chatappwithwebssocketcmp

import android.app.Application
import com.raouf_developer.chatappwithwebssocketcmp.di.networkModule
import com.raouf_developer.chatappwithwebssocketcmp.di.viewModelModule

import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication

class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@ChatApplication)
            modules(networkModule,viewModelModule)
        }
    }
}