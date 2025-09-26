package com.raouf_developer.chatappwithwebssocketcmp

import com.raouf_developer.chatappwithwebssocketcmp.di.networkModule
import com.raouf_developer.chatappwithwebssocketcmp.di.viewModelModule
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(
            networkModule,viewModelModule
        )
    }
}