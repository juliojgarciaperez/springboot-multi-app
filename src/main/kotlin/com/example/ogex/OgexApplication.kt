package com.example.ogex

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication @ComponentScan("com.example.ogex.common") open class OgexApplication

@ComponentScan("com.example.ogex.apps.foo") class FooApplication : OgexApplication()

@ComponentScan("com.example.ogex.apps.fooworker") class FooWorkerApplication : OgexApplication()

@ComponentScan("com.example.ogex.apps.bar") class BarApplication : OgexApplication()

fun main(args: Array<String>) {
    val ogexApp = System.getenv("SPRING_PROFILES_ACTIVE")

    when (ogexApp) {
        "bar" -> runApplication<BarApplication>(*args)
        "foo" -> runApplication<FooApplication>(*args)
        "fooworker" -> runApplication<FooWorkerApplication>(*args)
        else -> {
            println("No valid application profile set. Please set SPRING_PROFILES_ACTIVE")
            System.exit(1)
        }
    }
}
