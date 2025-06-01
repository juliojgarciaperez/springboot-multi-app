package com.example.ogex.common.rabbit

import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class RabbitSender(private val rabbitTemplate: RabbitTemplate) {
    private val logger = org.slf4j.LoggerFactory.getLogger(RabbitSender::class.java)

    fun sendToQueue(queueName: String, message: String) {
        logger.info("Sending message to queue '$queueName': $message")
        rabbitTemplate.convertAndSend(queueName, message)
    }
}
