package com.example.ogex.apps.fooworker

import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service

@Configuration
class FooConfig {
    @Bean fun fooQueue(): Queue = Queue("fooQueue", true)
}

@Service
class FooWorker {
    private val logger = LoggerFactory.getLogger(FooWorker::class.java)

    @RabbitListener(queues = ["fooQueue"])
    fun receiveMessage(message: String) {
        logger.info("Mensaje recibido de cola 'fooQueue': $message")
    }
}
