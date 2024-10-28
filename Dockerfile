# Use an official Gradle image with Java 21 for building the application
FROM gradle:jdk21 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle

RUN gradle dependencies --no-daemon

COPY src /app/src

RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21.0.5_11-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
