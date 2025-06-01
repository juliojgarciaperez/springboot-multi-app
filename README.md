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
