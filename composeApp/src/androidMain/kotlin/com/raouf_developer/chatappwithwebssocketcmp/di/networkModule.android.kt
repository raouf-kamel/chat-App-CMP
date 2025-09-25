package com.raouf_developer.chatappwithwebssocketcmp.di

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.okhttp.OkHttp
actual fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> {
    return OkHttp
}