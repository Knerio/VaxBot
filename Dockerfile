# Use an official Gradle image with Java 21 for building the application
FROM gradle:8.2-jdk21 AS builder

# Set the working directory
WORKDIR /app

# Copy build files
COPY build.gradle.kts settings.gradle.kts /app/
COPY gradle /app/gradle

# Download dependencies
RUN gradle dependencies --no-daemon

# Copy the rest of the application source code
COPY src /app/src

# Build the application using the shadowJar task to create a fat JAR
RUN gradle shadowJar --no-daemon

# Use a minimal base image to reduce final image size
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the application's port (if known, e.g., 8080)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
