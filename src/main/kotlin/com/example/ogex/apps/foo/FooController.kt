package com.example.ogex.apps.foo

import com.example.ogex.common.rabbit.RabbitSender
import com.example.ogex.common.say
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FooController(private val rabbitSender: RabbitSender) {

    @GetMapping("/foo")
    fun getFoo(): Map<String, String> {
        val uuid = java.util.UUID.randomUUID().toString()
        rabbitSender.sendToQueue("fooQueue", "Hello from FooController: $uuid")

        return mapOf("message" to say("foo"))
    }
}
