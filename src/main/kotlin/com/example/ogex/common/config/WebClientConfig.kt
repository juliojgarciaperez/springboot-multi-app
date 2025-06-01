package com.example.ogex.common.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    private val logger = LoggerFactory.getLogger(WebClientConfig::class.java)

    @Bean
    fun webClient(): WebClient =
            WebClient.builder()
                    .filter(addCorrelatorHeader())
                    .filter(logRequestAndResponse())
                    .build()

    private fun logRequestAndResponse(): ExchangeFilterFunction =
            ExchangeFilterFunction { request, next ->
                val startTime = System.currentTimeMillis()

                logger.info("[Request]: {} {}", request.method(), request.url())

                next.exchange(request).doOnNext { response ->
                    val duration = System.currentTimeMillis() - startTime

                    logger.info(
                            "[Response]: {} {} - {} ({}ms)",
                            request.method(),
                            request.url(),
                            response.statusCode(),
                            duration
                    )
                }
            }

    private fun addCorrelatorHeader(): ExchangeFilterFunction =
            ExchangeFilterFunction { request, next ->
                // val correlator = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY)

                // request.headers().add("X-Correlator", correlator ?: "unknown")

                next.exchange(request)
            }
}
