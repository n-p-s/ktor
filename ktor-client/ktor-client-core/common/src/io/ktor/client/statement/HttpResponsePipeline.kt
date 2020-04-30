/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.statement

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.util.pipeline.*

/**
 * [HttpClient] Pipeline used for executing [HttpResponse].
 */
public class HttpResponsePipeline : Pipeline<HttpResponseContainer, HttpClientCall>(
    Receive,
    Parse,
    Transform,
    State,
    After
) {
    public companion object Phases {
        /**
         * The earliest phase that happens before any other
         */
        public val Receive = PipelinePhase("Receive")

        /**
         * Decode response body
         */
        public val Parse = PipelinePhase("Parse")

        /**
         * Transform response body to expected format
         */
        public val Transform = PipelinePhase("Transform")

        /**
         * Use this phase to store request shared state
         */
        public val State = PipelinePhase("State")

        /**
         * Latest response pipeline phase
         */
        public val After = PipelinePhase("After")
    }
}

/**
 * [HttpClient] Pipeline used for receiving [HttpResponse] without any processing.
 */
public class HttpReceivePipeline : Pipeline<HttpResponse, HttpClientCall>(
    Before,
    State,
    After
) {
    public companion object Phases {
        /**
         * The earliest phase that happens before any other
         */
        public val Before = PipelinePhase("Before")

        /**
         * Use this phase to store request shared state
         */
        public val State = PipelinePhase("State")

        /**
         * Latest response pipeline phase
         */
        public val After = PipelinePhase("After")
    }
}

/**
 * Class representing a typed [response] with an attached [expectedType].
 * @param expectedType: information about expected type.
 * @param response: current response state.
 */
public data class HttpResponseContainer(val expectedType: TypeInfo, val response: Any)
