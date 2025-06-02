# Spring Boot Multi-App Project

This project demonstrates how to work with a mono-repo/mono-project approach in Spring Boot, while designing for a microservices architecture.

## Structure

- Multiple independent "apps" are located under `com.example.ogex.apps`.  
  Each app has its own configuration file in the `resources` directory, and all inherit from a shared `application.properties` for common settings.
- Each app configures its own database connection (if needed), RabbitMQ queue, etc., in its specific configuration file.
- The app to launch is selected via the `SPRING_PROFILES_ACTIVE` environment variable.
- An architecture test ensures there are no cross-dependencies between apps.
- CI can run tests only for the service that has changed, using `git diff`.
- CI can also add labels to PRs or prefix to commit messages, identifying what app changed using `git diff`
- Apps must communicate with each other using microservice interfaces: queues, HTTP, etc.  
  One app does **must not** access another app's database (except for API/WORKER patterns).
- The `com.example.ogex.common` package contains code shared by all apps: HTTP clients, logging, caching, metrics, utilities, etc.

## Build & Deployment

- The entire project is compiled, and the Docker image contains all code in a single JAR.
- However, Spring Boot only loads the app specified by the profile into memory.  
  Code from other apps is not executed or loaded in any way.
- This approach is practical for teams that do not expect the codebase to grow so large that having all sources in the JAR becomes a problem.
- Maintaining a single Docker image is simpler than managing many.

## Advantages

- Focus on a single repository for a small team.
- Easier dependency management and CI/CD.
- Shared documentation and code reuse without the need to version or publish libraries to a repository manager.
- Deprecating an app is as simple as deleting its package and configuration file.

---

This setup allows for efficient development and maintenance of multiple microservices within a single Spring Boot project, leveraging shared resources and streamlined workflows.

## RabbitMQ and Retry Mechanism

Async tasks using RabbitMQ and retries with delay, without blocking spring app threads.

Check example on [FooWorker.kt](https://github.com/juliojgarciaperez/springboot-multi-app/blob/main/src/main/kotlin/com/example/ogex/apps/fooworker/FooWorker.kt)

```kt
@Configuration
@EnableRabbit
class RabbitMQConfig {

    @Bean
    fun mainQueue(): Queue {
        return QueueBuilder.durable("main.queue")
            .withArgument("x-dead-letter-exchange", "") // default exchange
            .withArgument("x-dead-letter-routing-key", "main.queue.dlq") // error messages go to DLQ
            .build()
    }

    @Bean
    fun deadLetterQueue(): Queue {
        return QueueBuilder.durable("main.queue.dql")
            .withArgument("x-message-ttl", 10_000) // messages expire after 10 seconds
            .withArgument("x-dead-letter-exchange", "") // default exchange
            .withArgument("x-dead-letter-routing-key", "main.queue") // error messages go back to main queue
            .build() // this queue won't have any workers consuming from it so all messages will be sent back to the main queue
    }
}

@Component
class RabbitMQListener {
    private val logger = org.slf4j.LoggerFactory.getLogger(RabbitMQListener::class.java)

    @RabbitListener(queues = ["main.queue"])
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
```

### IDEA

Given a main queue "X" and its associated Dead Letter Queue "X.DLQ" in RabbitMQ:

1. When a message is processed successfully, it is acknowledged and removed from queue X.

2. If message processing fails and default-requeue-rejected is disabled, the message is rejected and routed to X.DLQ.

3. The X.DLQ queue is configured with a TTL (Time-To-Live), causing messages to expire after a set duration.

4. No consumers will process messages directly from X.DLQ. Instead, X.DLQ is configured with "X" as its own Dead Letter Exchange (DLX), so expired messages are automatically routed back to the main queue X.

5. Upon re-entry to X, each message will include an x-death header containing metadata such as the number of delivery attempts.

6. The message handler can inspect this header to determine how many times the message has been retried and decide whether to continue processing or discard the message.

### Auto Configuration

TODO: think on how to create queue and dlq automatically, based on configuration file.
