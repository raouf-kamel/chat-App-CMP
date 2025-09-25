package com.raouf_developer.chatappwithwebssocketcmp.di

import com.raouf_developer.chatappwithwebssocketcmp.presentation.ChatViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val viewModelModule = module {
    viewModelOf(::ChatViewModel)

}