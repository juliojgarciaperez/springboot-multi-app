FROM eclipse-temurin:20.0.2_9-jdk

WORKDIR /app

COPY target/ogex-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]