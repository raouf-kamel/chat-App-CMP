package com.raouf_developer.chatappwithwebssocketcmp.di

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun createHttpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> {
    return Darwin
}