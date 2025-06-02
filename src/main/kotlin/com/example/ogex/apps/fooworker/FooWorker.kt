package com.example.ogex.apps.fooworker

import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import org.springframework.messaging.handler.annotation.Header

@Configuration
@EnableRabbit
class RabbitMQConfig {

    companion object {
        const val MAIN_QUEUE = "main.queue"
        const val DLQ_QUEUE = "main.queue.dlq"
        const val DLX_EXCHANGE = "dlx.exchange"
    }

    @Bean
    fun mainQueue(): Queue {
        return QueueBuilder.durable(MAIN_QUEUE)
            .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DLQ_QUEUE) // error messages go to DLQ
            .build()
    }

    @Bean
    fun deadLetterQueue(): Queue {
        return QueueBuilder.durable(DLQ_QUEUE)
            .withArgument("x-message-ttl", 10_000) // messages expire after 10 seconds
            .withArgument("x-dead-letter-exchange", "") 
            .withArgument("x-dead-letter-routing-key", MAIN_QUEUE) // error messages go back to main queue
            .build() // this queue won't have any workers consuming from it so all messages will be sent back to the main queue
    }

    @Bean
    fun deadLetterExchange(): DirectExchange {
        return DirectExchange(DLX_EXCHANGE)
    }

    @Bean
    fun deadLetterBinding(): Binding {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with(DLQ_QUEUE)
    }
}

@Component
class RabbitMQListener {
    private val logger = org.slf4j.LoggerFactory.getLogger(RabbitMQListener::class.java)

    @RabbitListener(queues = [ RabbitMQConfig.MAIN_QUEUE])
    fun procesarMensaje(
        mensaje: String,
        @Header("x-death", required = false) xDeath: List<Map<String, Any>>?
    ) {
        val retries = xDeath?.firstOrNull()?.get("count") as? Long ?: 0L

        logger.info("RabbitListener. Received: $mensaje (retries: $retries)")

        if (retries < 3) {
            logger.info("Error!. reject message")
            throw RuntimeException("Mensaje con error: $mensaje")
        } 

        logger.info("retries over. alarm and discard message")
    }
}
