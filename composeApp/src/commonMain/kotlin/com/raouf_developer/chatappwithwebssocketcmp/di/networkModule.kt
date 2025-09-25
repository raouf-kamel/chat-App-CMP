package com.raouf_developer.chatappwithwebssocketcmp.di

import com.raouf_developer.chatappwithwebssocketcmp.network.ApiConstants.BASE_URL
import com.raouf_developer.chatappwithwebssocketcmp.network.ChatService
import com.raouf_developer.chatappwithwebssocketcmp.network.createHttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

internal val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
            isLenient = true
        }
    }
    single(named("baseUrl")) { BASE_URL }
    single { createHttpClientEngine() }
    single { createHttpClient(get(named("baseUrl")), get()) }
    single { ChatService(get(), get()) }
}

expect fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>