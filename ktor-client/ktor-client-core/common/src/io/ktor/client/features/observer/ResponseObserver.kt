/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features.observer

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.*

/**
 * [ResponseObserver] callback.
 */
public typealias ResponseHandler = suspend (HttpResponse) -> Unit

/**
 * Observe response feature.
 */
public class ResponseObserver(
    private val responseHandler: ResponseHandler
) {
    public class Config {
        internal var responseHandler: ResponseHandler = {}

        /**
         * Set response handler for logging.
         */
        public fun onResponse(block: ResponseHandler) {
            responseHandler = block
        }
    }

    public companion object Feature : HttpClientFeature<Config, ResponseObserver> {

        override val key: AttributeKey<ResponseObserver> = AttributeKey("BodyInterceptor")

        override fun prepare(block: Config.() -> Unit): ResponseObserver =
            ResponseObserver(Config().apply(block).responseHandler)

        override fun install(feature: ResponseObserver, scope: HttpClient) {

            scope.receivePipeline.intercept(HttpReceivePipeline.After) { response ->
                val (loggingContent, responseContent) = response.content.split(response)

                val newClientCall = context.wrapWithContent(responseContent)
                val sideCall = newClientCall.wrapWithContent(loggingContent)

                scope.launch {
                    feature.responseHandler(sideCall.response)
                }

                context.response = newClientCall.response
                context.request = newClientCall.request

                @Suppress("UNCHECKED_CAST")
                (response.coroutineContext[Job] as CompletableJob).complete()
                proceedWith(context.response)
            }
        }
    }
}

/**
 * Install [ResponseObserver] feature in client.
 */
public fun HttpClientConfig<*>.ResponseObserver(block: ResponseHandler) {
    install(ResponseObserver) {
        responseHandler = block
    }
}
